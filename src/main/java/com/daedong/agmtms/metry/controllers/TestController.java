package com.daedong.agmtms.metry.controllers;

import com.daedong.agmtms.metry.services.IotMessageService;
import com.daedong.agmtms.metry.dto.IotMessageDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
public class TestController {
    
    @Autowired
    private IotMessageService messageService;
    
    @GetMapping("/test")
    public String test() {
        return "Hello World! Application is running.";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/test/count")
    public String getMessageCount() {
        int count = messageService.getTotalMessageCount();
        return "Total messages in database: " + count;
    }
    
    @GetMapping("/test/messages")
    public String getMessages() {
        List<IotMessageDto> messages = messageService.getAllMessages();
        return "Found " + messages.size() + " messages";
    }
    
    @GetMapping("/test/messages-simple")
    public String getMessagesSimple() {
        List<IotMessageDto> messages = messageService.getAllMessagesSimple();
        return "Found " + messages.size() + " messages (simple)";
    }
    
    @GetMapping("/test/messages-direct")
    public String getMessagesDirect() {
        List<IotMessageDto> messages = messageService.getAllMessagesDirect();
        return "Found " + messages.size() + " messages (direct JDBC)";
    }
    
    @GetMapping("/test/table-info")
    public String getTableInfo() {
        return messageService.getTableInfo();
    }
    
    @GetMapping("/test/db-connection")
    public String testDatabaseConnection() {
        return messageService.testDatabaseConnection();
    }
    
    @GetMapping("/test/simple-query")
    public String testSimpleQuery() {
        return messageService.testSimpleQuery();
    }
    
    @GetMapping("/test/messages-simple-direct")
    public String getMessagesSimpleDirect() {
        List<IotMessageDto> messages = messageService.getAllMessagesSimpleDirect();
        return "Found " + messages.size() + " messages (simple direct)";
    }
} 