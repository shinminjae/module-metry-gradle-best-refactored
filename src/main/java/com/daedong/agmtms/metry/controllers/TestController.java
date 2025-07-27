package com.daedong.agmtms.metry.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "Hello World! Application is running.";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
} 