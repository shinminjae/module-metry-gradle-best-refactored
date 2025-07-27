
package com.daedong.agmtms.metry.services;

import com.daedong.agmtms.metry.dao.IotMessageMapper;
import com.daedong.agmtms.metry.dto.IotMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class IotMessageService {
    private final IotMessageMapper mapper;
    
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    public void saveMessage(IotMessageDto dto) {
        if (dto.getCreatedAt() == null) {
            dto.setCreatedAt(LocalDateTime.now());
        }
        mapper.insertMessage(dto);
        cleanupOldMessages();
    }

    public List<IotMessageDto> searchMessages(LocalDateTime startDate, LocalDateTime endDate,
                                              String deviceId, String topic, String dataType,
                                              Double minValue, Double maxValue,
                                              int page, int size, String sort) {
        try {
            int offset = (page - 1) * size;
            List<IotMessageDto> result = mapper.searchMessages(startDate, endDate, deviceId, topic, dataType, minValue, maxValue, offset, size, sort);
            System.out.println("Search result size: " + (result != null ? result.size() : "null"));
            return result;
        } catch (Exception e) {
            System.err.println("Error in searchMessages: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    private void cleanupOldMessages() {
        int totalCount = mapper.countMessages();
        if (totalCount > 1000) {
            int deleteCount = totalCount - 1000;
            mapper.deleteOldestMessages(deleteCount);
        }
    }
    
    public int getTotalMessageCount() {
        try {
            return mapper.countMessages();
        } catch (Exception e) {
            System.err.println("Error getting message count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    public List<IotMessageDto> getAllMessages() {
        try {
            return mapper.searchMessages(null, null, null, null, null, null, null, 1, 50, "DESC");
        } catch (Exception e) {
            System.err.println("Error getting all messages: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public List<IotMessageDto> getAllMessagesSimple() {
        try {
            return mapper.getAllMessagesSimple();
        } catch (Exception e) {
            System.err.println("Error getting all messages simple: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public List<IotMessageDto> getAllMessagesDirect() {
        List<IotMessageDto> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, device_id, topic, payload, data_type, value, received_at, raw_message, created_at FROM iot_messages ORDER BY received_at DESC LIMIT 50")) {
            
            System.out.println("Executing query: SELECT id, device_id, topic, payload, data_type, value, received_at, raw_message, created_at FROM iot_messages ORDER BY received_at DESC LIMIT 50");
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    // 간단한 DTO 생성 (빌더 패턴 대신 생성자 사용)
                    IotMessageDto dto = new IotMessageDto();
                    dto.setId(rs.getLong("id"));
                    dto.setDeviceId(rs.getString("device_id"));
                    dto.setTopic(rs.getString("topic"));
                    dto.setPayload(rs.getString("payload"));
                    dto.setDataType(rs.getString("data_type"));
                    
                    Object valueObj = rs.getObject("value");
                    if (valueObj != null) {
                        dto.setValue(Double.valueOf(valueObj.toString()));
                    }
                    
                    Object receivedAtObj = rs.getObject("received_at");
                    if (receivedAtObj != null) {
                        dto.setReceivedAt(((java.sql.Timestamp) receivedAtObj).toLocalDateTime());
                    }
                    
                    Object createdAtObj = rs.getObject("created_at");
                    if (createdAtObj != null) {
                        dto.setCreatedAt(((java.sql.Timestamp) createdAtObj).toLocalDateTime());
                    }
                    
                    dto.setRawMessage(rs.getString("raw_message"));
                    messages.add(dto);
                    System.out.println("Successfully built DTO for row " + rowCount + ": " + dto.getDeviceId() + " - " + dto.getTopic());
                } catch (Exception e) {
                    System.err.println("Error building DTO for row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Total rows processed: " + rowCount + ", messages built: " + messages.size());
        } catch (Exception e) {
            System.err.println("Error getting messages directly: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
    
    public List<IotMessageDto> getAllMessagesSimpleDirect() {
        List<IotMessageDto> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, device_id, topic FROM iot_messages ORDER BY received_at DESC LIMIT 50")) {
            
            System.out.println("Executing simple query: SELECT id, device_id, topic FROM iot_messages ORDER BY received_at DESC LIMIT 50");
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    // 최소한의 DTO 생성
                    IotMessageDto dto = new IotMessageDto();
                    dto.setId(rs.getLong("id"));
                    dto.setDeviceId(rs.getString("device_id"));
                    dto.setTopic(rs.getString("topic"));
                    messages.add(dto);
                    System.out.println("Successfully built simple DTO for row " + rowCount + ": " + dto.getDeviceId() + " - " + dto.getTopic());
                } catch (Exception e) {
                    System.err.println("Error building simple DTO for row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Total rows processed: " + rowCount + ", simple messages built: " + messages.size());
        } catch (Exception e) {
            System.err.println("Error getting simple messages directly: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
    
    public String testSimpleQuery() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, device_id, topic FROM iot_messages LIMIT 5")) {
            
            StringBuilder result = new StringBuilder();
            result.append("Simple query test:\n");
            int count = 0;
            while (rs.next()) {
                count++;
                result.append("Row ").append(count).append(": ")
                      .append("id=").append(rs.getLong("id"))
                      .append(", device_id=").append(rs.getString("device_id"))
                      .append(", topic=").append(rs.getString("topic"))
                      .append("\n");
            }
            result.append("Total rows: ").append(count);
            return result.toString();
        } catch (Exception e) {
            return "Error in simple query: " + e.getMessage();
        }
    }
    
    public String getTableInfo() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(iot_messages)")) {
            
            StringBuilder info = new StringBuilder();
            info.append("Table structure:\n");
            while (rs.next()) {
                info.append("Column: ").append(rs.getString("name"))
                    .append(", Type: ").append(rs.getString("type"))
                    .append(", NotNull: ").append(rs.getBoolean("notnull"))
                    .append(", Default: ").append(rs.getString("dflt_value"))
                    .append("\n");
            }
            return info.toString();
        } catch (Exception e) {
            return "Error getting table info: " + e.getMessage();
        }
    }
    
    public String testDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM iot_messages")) {
            
            if (rs.next()) {
                int count = rs.getInt("count");
                return "Database connection successful. Total messages: " + count;
            } else {
                return "Database connection successful but no data found.";
            }
        } catch (Exception e) {
            return "Database connection error: " + e.getMessage();
        }
    }
}
