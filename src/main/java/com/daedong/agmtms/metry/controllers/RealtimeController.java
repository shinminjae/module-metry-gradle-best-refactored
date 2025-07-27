
package com.daedong.agmtms.metry.controllers;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RealtimeController {
    private final IotMessageService service;

    @GetMapping("/realtime")
    public List<IotMessageDto> getRealtimeMessages() {
        return service.searchMessages(null, null, null, null, null,
                null, null, 1, 50, "DESC");
    }
}
