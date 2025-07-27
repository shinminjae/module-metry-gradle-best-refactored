
package com.daedong.agmtms.metry.services;

import com.daedong.agmtms.metry.dao.IotMessageMapper;
import com.daedong.agmtms.metry.dto.IotMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IotMessageService {
    private final IotMessageMapper mapper;

    public void saveMessage(IotMessageDto dto) {
        mapper.insertMessage(dto);
        cleanupOldMessages();
    }

    public List<IotMessageDto> searchMessages(LocalDateTime startDate, LocalDateTime endDate,
                                              String deviceId, String topic, String dataType,
                                              Double minValue, Double maxValue,
                                              int page, int size, String sort) {
        int offset = (page - 1) * size;
        return mapper.searchMessages(startDate, endDate, deviceId, topic, dataType, minValue, maxValue, offset, size, sort);
    }

    private void cleanupOldMessages() {
        int totalCount = mapper.countMessages();
        if (totalCount > 1000) {
            int deleteCount = totalCount - 1000;
            mapper.deleteOldestMessages(deleteCount);
        }
    }
}
