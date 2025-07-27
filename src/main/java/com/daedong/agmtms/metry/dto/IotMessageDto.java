
package com.daedong.agmtms.metry.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IotMessageDto {
    private Long id;
    private String deviceId;
    private String topic;
    private String payload;
    private String dataType;
    private Double value;
    private LocalDateTime receivedAt;
    private String rawMessage;
    private LocalDateTime createdAt;
}
