package com.daedong.agmtms.config;

import com.daedong.agmtms.common.AwsIotCoreSubscribe;
import com.daedong.agmtms.metry.service.map.MapMetryService;
import com.daedong.agmtms.metry.service.trip.TripMetryService;
import com.daedong.agmtms.metry.service.acc.AccMetryService;
import com.daedong.agmtms.metry.service.dtc.DtcMetryService;
import com.daedong.agmtms.metry.service.init.InitMetryService;
import com.daedong.agmtms.metry.service.ro.RoMetryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket;
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@Slf4j
public class AwsIotCoreConfig {

    @Value("${aws.iot.endpoint}")
    private String endPoint;
    
    @Value("${aws.iot.clientId}")
    private String clientId;
    
    @Value("${aws.iot.certificateFile}")
    private String certFile;
    
    @Value("${aws.iot.privateKeyFile}")
    private String keyFile;

    @Value("#{'${aws.iot.topics}'.split(',')}")
    private List<String> topicInfo;

    @Resource(name = "initMetryService")
    private InitMetryService initMetryService;

    @Resource(name = "dtcMetryService")
    private DtcMetryService dtcMetryService;

    @Resource(name = "roMetryService")
    private RoMetryService roMetryService;

    @Resource(name = "tripMetryService")
    private TripMetryService tripMetryService;

    @Resource(name = "mapMetryService")
    private MapMetryService mapMetryService;

    @Resource(name = "accMetryService")
    private AccMetryService accMetryService;
    
    static String ciPropValue = System.getProperty("aws.crt.ci");
    static boolean isCI = ciPropValue != null && Boolean.valueOf(ciPropValue);

    Mqtt5Client client = null;

    @Bean
    public Mqtt5Client getConnection() {
        log.info("Aws Iot Core connect=============================");

        try {
            AwsIotMqtt5ClientBuilder builder = AwsIotMqtt5ClientBuilder.newDirectMqttBuilderWithMtlsFromPath(endPoint, certFile, keyFile);
            ConnectPacket.ConnectPacketBuilder connectProperties = new ConnectPacket.ConnectPacketBuilder();
            connectProperties.withClientId(clientId);
            connectProperties.withSessionExpiryIntervalSeconds(3600L);
            connectProperties.withKeepAliveIntervalSeconds(600L);

            builder.withSessionBehavior(Mqtt5ClientOptions.ClientSessionBehavior.REJOIN_ALWAYS);
            builder.withConnectProperties(connectProperties);
            builder.withLifeCycleEvents(new SampleLifecycleEvents(topicInfo));
            builder.withPublishEvents(new AwsIotCoreSubscribe.AwsIotCoreSubEvents(
                    initMetryService, dtcMetryService, roMetryService, tripMetryService, accMetryService, mapMetryService));
            client = builder.build();
        } catch (CrtRuntimeException ex) {
            log.error("CrtRuntimeException: {}", ex.getMessage(), ex);
            onApplicationFailure(ex);
        } catch (Exception e) {
            log.error("Exception during connection setup: {}", e.getMessage(), e);
        }

        if (client != null) {
            client.start();
        }
        return client;
    }

    static void onApplicationFailure(Throwable cause) {
        if (isCI) {
            throw new RuntimeException("Mqtt5 PubSub: execution failure", cause);
        } else if (cause != null) {
            log.error("Exception encountered: {}", cause.toString());
        }
    }
    
    // 생명주기 이벤트 핸들러
    static final class SampleLifecycleEvents implements Mqtt5ClientOptions.LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> stoppedFuture = new CompletableFuture<>();

        private List<String> topicInfo;

        public SampleLifecycleEvents(List<String> topicInfo) {
            this.topicInfo = topicInfo;
        }
        
        //연결시도
        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {
            log.info("Mqtt5 Client: Attempting connection...");
        }

        //연결성공
        @Override
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
            log.info("Mqtt5 Client: Connection success == topicInfo: {}, client ID: {}"
                , topicInfo
                , onConnectionSuccessReturn.getNegotiatedSettings().getAssignedClientID());

            try {
                // 직접 구독 처리
                for (String topic : topicInfo) {
                    client.subscribe(new SubscribePacket.SubscribePacketBuilder()
                        .withSubscription(topic, QOS.AT_LEAST_ONCE)
                        .build());
                    log.info("Subscribed to topic: {}", topic);
                }

                connectedFuture.complete(null);
            } catch (CrtRuntimeException ce) {
                log.error("CrtRuntimeException during subscription: {}", ce.getMessage(), ce);
            }
            log.info("getSubStart =======> restart : {}", client.getIsConnected());
        }

        //연결실패
        @Override
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {
            String errorString = CRT.awsErrorString(onConnectionFailureReturn.getErrorCode());
            log.error("Mqtt5 Client: Connection failed with error: {}", errorString);
            connectedFuture.completeExceptionally(new Exception("Could not connect: " + errorString));
        }

        //연결해제
        @Override
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {
            log.info("Mqtt5 Client: Disconnected");
            DisconnectPacket disconnectPacket = onDisconnectionReturn.getDisconnectPacket();
            if (disconnectPacket != null) {
                log.info("\tDisconnection packet code: {}", disconnectPacket.getReasonCode());
                log.info("\tDisconnection packet reason: {}", disconnectPacket.getReasonString());
            }
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
            log.info("Mqtt5 Client: Stopped");
        }
    }

    private String clientUUID(String key) {
        String ranStr = java.util.UUID.randomUUID().toString().substring(0, 8);
        log.info("Generated client UUID: {}-{}", key, ranStr);
        return key + ranStr;
    }
} 