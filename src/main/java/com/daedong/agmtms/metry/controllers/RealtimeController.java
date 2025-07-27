
package com.daedong.agmtms.metry.controllers;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RealtimeController {
    private final IotMessageService service;

    // 실시간 모니터링 페이지 (HTML)
    @GetMapping("/realtime")
    public String realtimePage(Model model) {
        try {
            // 최근 50개 메시지를 모델에 추가
            List<IotMessageDto> messages = service.getAllMessagesSimpleDirect();
            model.addAttribute("messages", messages);
        } catch (Exception e) {
            // 오류 발생 시 빈 리스트로 초기화
            model.addAttribute("messages", new java.util.ArrayList<>());
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "realtime";
    }

    // 실시간 데이터 API (JSON)
    @GetMapping("/api/realtime")
    @ResponseBody
    public List<IotMessageDto> getRealtimeMessages() {
        try {
            return service.getAllMessagesSimpleDirect();
        } catch (Exception e) {
            // 오류 발생 시 빈 리스트 반환
            return new java.util.ArrayList<>();
        }
    }
}
