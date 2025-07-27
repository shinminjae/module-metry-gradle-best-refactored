package com.daedong.agmtms.metry.service.acc;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("accMetryService")
@RequiredArgsConstructor
@Slf4j
public class AccMetryService {
    
    private final IotMessageService messageService;
    
    public void processAccMessage(String topic, String payload) {
        try {
            log.info("Processing ACC message: topic={}, payload={}", topic, payload);
            
            String[] parts = topic.split("/");
            String deviceId = parts.length > 2 ? parts[2] : "UNKNOWN";
            
            IotMessageDto dto = IotMessageDto.builder()
                    .deviceId(deviceId)
                    .topic(topic)
                    .payload(payload)
                    .dataType("ACC")
                    .value(null)
                    .receivedAt(LocalDateTime.now())
                    .rawMessage(payload)
                    .build();
            
            messageService.saveMessage(dto);
        } catch (Exception e) {
            log.error("Error processing ACC message", e);
        }
    }
} 