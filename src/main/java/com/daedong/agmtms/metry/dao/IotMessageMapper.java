
package com.daedong.agmtms.metry.dao;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IotMessageMapper {
    void insertMessage(IotMessageDto message);

    List<IotMessageDto> searchMessages(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("deviceId") String deviceId,
                                       @Param("topic") String topic,
                                       @Param("dataType") String dataType,
                                       @Param("minValue") Double minValue,
                                       @Param("maxValue") Double maxValue,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit,
                                       @Param("sort") String sort);

    int countMessages();

    void deleteOldestMessages(@Param("deleteCount") int deleteCount);
}
