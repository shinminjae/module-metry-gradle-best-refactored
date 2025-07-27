
package com.daedong.agmtms.common;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

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

    @PostConstruct
    public void subscribe() {
        try {
            validateCertificates();

            log.info("Using certPath: {}", certPath);
            log.info("Using keyPath: {}", keyPath);
            log.info("Using rootCaPath: {}", rootCaPath);
            log.info("Using endpoint: {}", endpoint);
            log.info("Using clientId: {}", clientId);
            log.info("Subscribing to topics: {}", topics);

            AwsIotMqttConnectionBuilder builder =
                    AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath);

            builder.withCertificateAuthorityFromPath(null, rootCaPath)
                    .withEndpoint(endpoint)
                    .withClientId(clientId + "-" + UUID.randomUUID())
                    .withCleanSession(true);

            try (MqttClientConnection connection = builder.build()) {
                CompletableFuture<Boolean> connected = connection.connect();
                connected.whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[AWS-IOT-ERROR] ","AWS IoT Connection error: {} - {}", error.getClass().getName(), error.getMessage(), error);
                    } else if (!result) {
                        log.error("[AWS-IOT-ERROR] ","Connection failed. Check your network, endpoint, certificate, and AWS IoT policy.. Check endpoint, certificate, and policy.");
                    } else {
                        log.info("Connected to AWS IoT Core");
                        topics.forEach(topic -> {
                            try {
                                connection.subscribe(topic, QualityOfService.AT_LEAST_ONCE,
                                        message -> processMessage(message.getTopic(), message.getPayload()));
                                log.info("Subscribed to topic: {}", topic);
                            } catch (Exception e) {
                                log.error("[AWS-IOT-ERROR] ","Failed to subscribe to topic {}: {}", topic, e.getMessage(), e);
                            }
                        });
                    }
                });

                while (true) {
                    Thread.sleep(10000);
                }
            }
        } catch (Exception e) {
            log.error("[AWS-IOT-ERROR] ","AWS IoT Subscribe fatal error: {} - {}", e.getClass().getName(), e.getMessage(), e);
        }
    }

    private void validateCertificates() {
        validatePemFile(certPath, "Device Certificate");
        validatePemFile(keyPath, "Private Key");
        validatePemFile(rootCaPath, "Root CA");
    }

    private void validatePemFile(String path, String name) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException(name + " file not found: " + path);
        }
        try {
            String content = Files.readString(file.toPath());
            if (!content.contains("-----BEGIN") || !content.contains("-----END")) {
                throw new RuntimeException(name + " is not a valid PEM format: " + path);
            }
            log.info("{} validated successfully: {}", name, path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + name + ": " + path, e);
        }
    }

    private void processMessage(String topic, byte[] payload) {
        try {
            String message = new String(payload, StandardCharsets.UTF_8);
            log.info("Received :: topic={} payload={}", topic, message);

            String[] parts = topic.split("/");
            String dataType = parts.length > 1 ? parts[1] : "UNKNOWN";
            String deviceId = parts.length > 2 ? parts[2] : "UNKNOWN";

            IotMessageDto dto = IotMessageDto.builder()
                    .deviceId(deviceId)
                    .topic(topic)
                    .payload(message)
                    .dataType(dataType)
                    .value(null)
                    .receivedAt(LocalDateTime.now())
                    .rawMessage(message)
                    .build();

            messageService.saveMessage(dto);
        } catch (Exception e) {
            log.error("[AWS-IOT-ERROR] ","Error processing message", e);
        }
    }
}
