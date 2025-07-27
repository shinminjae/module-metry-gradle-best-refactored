
package com.daedong.agmtms.common;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import com.daedong.agmtms.metry.service.acc.AccMetryService;
import com.daedong.agmtms.metry.service.dtc.DtcMetryService;
import com.daedong.agmtms.metry.service.init.InitMetryService;
import com.daedong.agmtms.metry.service.map.MapMetryService;
import com.daedong.agmtms.metry.service.ro.RoMetryService;
import com.daedong.agmtms.metry.service.trip.TripMetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket;
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsIotCoreSubscribe {

    private final IotMessageService messageService;

    @Value("${aws.iot.endpoint}")
    private String endpoint;

    @Value("${aws.iot.clientId}")
    private String clientId;

    @Value("#{'${aws.iot.topics}'.split(',')}")
    private List<String> topics;

    private final String certPath = "certs/MZC-Test-Thing-01.cert.pem";
    private final String keyPath = "certs/MZC-Test-Thing-01.private.key";
    private final String rootCaPath = "certs/AmazonRootCA1.pem";

    // MQTT5 기반 구독 메서드
    public void AwsIotCoreSub(Mqtt5Client client, List<String> topics) {
        try {
            log.info("Subscribing to topics: {}", topics);
            
            for (String topic : topics) {
                client.subscribe(new SubscribePacket.SubscribePacketBuilder()
                    .withSubscription(topic, QOS.AT_LEAST_ONCE)
                    .build());
                log.info("Subscribed to topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to subscribe to topics: {}", e.getMessage(), e);
        }
    }

    // MQTT5 이벤트 핸들러 클래스
    public static class AwsIotCoreSubEvents implements Mqtt5ClientOptions.PublishEvents {
        private final InitMetryService initMetryService;
        private final DtcMetryService dtcMetryService;
        private final RoMetryService roMetryService;
        private final TripMetryService tripMetryService;
        private final AccMetryService accMetryService;
        private final MapMetryService mapMetryService;

        public AwsIotCoreSubEvents(InitMetryService initMetryService, DtcMetryService dtcMetryService,
                                 RoMetryService roMetryService, TripMetryService tripMetryService,
                                 AccMetryService accMetryService, MapMetryService mapMetryService) {
            this.initMetryService = initMetryService;
            this.dtcMetryService = dtcMetryService;
            this.roMetryService = roMetryService;
            this.tripMetryService = tripMetryService;
            this.accMetryService = accMetryService;
            this.mapMetryService = mapMetryService;
        }

        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {
            PublishPacket publishPacket = publishReturn.getPublishPacket();
            String topic = publishPacket.getTopic();
            String payload = new String(publishPacket.getPayload(), StandardCharsets.UTF_8);
            
            log.info("Received message: topic={}, payload={}", topic, payload);
            
            // 토픽에 따라 적절한 서비스로 라우팅
            if (topic.contains("/INIT/")) {
                initMetryService.processInitMessage(topic, payload);
            } else if (topic.contains("/DTC/")) {
                dtcMetryService.processDtcMessage(topic, payload);
            } else if (topic.contains("/RO/")) {
                roMetryService.processRoMessage(topic, payload);
            } else if (topic.contains("/TRIP/")) {
                tripMetryService.processTripMessage(topic, payload);
            } else if (topic.contains("/ACC/")) {
                accMetryService.processAccMessage(topic, payload);
            } else if (topic.contains("/MAP/")) {
                mapMetryService.processMapMessage(topic, payload);
            } else {
                // 기본 처리
                processDefaultMessage(topic, payload);
            }
        }

        private void processDefaultMessage(String topic, String payload) {
            try {
                String[] parts = topic.split("/");
                String dataType = parts.length > 1 ? parts[1] : "UNKNOWN";
                String deviceId = parts.length > 2 ? parts[2] : "UNKNOWN";

                IotMessageDto dto = IotMessageDto.builder()
                        .deviceId(deviceId)
                        .topic(topic)
                        .payload(payload)
                        .dataType(dataType)
                        .value(null)
                        .receivedAt(LocalDateTime.now())
                        .rawMessage(payload)
                        .build();

                // 기본 메시지 서비스로 저장
                // messageService.saveMessage(dto);
            } catch (Exception e) {
                log.error("Error processing default message", e);
            }
        }
    }

    @PostConstruct
    public void subscribe() {
        log.info("AwsIotCoreSubscribe initialized - MQTT5 configuration will be handled by AwsIotCoreConfig");
    }
}
