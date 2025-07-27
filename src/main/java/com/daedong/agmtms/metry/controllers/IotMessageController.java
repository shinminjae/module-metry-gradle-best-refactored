
package com.daedong.agmtms.metry.controllers;

import com.daedong.agmtms.metry.dto.IotMessageDto;
import com.daedong.agmtms.metry.services.IotMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class IotMessageController {
    private final IotMessageService service;

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "redirect:/search";
    }

    // 검색 페이지 초기 로드
    @GetMapping("/search")
    public String searchPage(Model model) {
        try {
            // 초기 페이지 로드 시 최근 20개 메시지 표시
            List<IotMessageDto> result = service.getAllMessagesSimpleDirect();
            model.addAttribute("messages", result);
        } catch (Exception e) {
            // 오류 발생 시 빈 리스트로 초기화
            model.addAttribute("messages", new java.util.ArrayList<>());
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                         @RequestParam(required = false) String deviceId,
                         @RequestParam(required = false) String topic,
                         @RequestParam(required = false) String dataType,
                         @RequestParam(required = false) Double minValue,
                         @RequestParam(required = false) Double maxValue,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(defaultValue = "DESC") String sort,
                         Model model) {
        List<IotMessageDto> result = service.searchMessages(startDate, endDate, deviceId, topic, dataType, minValue, maxValue, page, size, sort);
        model.addAttribute("messages", result);
        return "search";
    }

    @GetMapping("/export")
    public void exportCSV(HttpServletResponse response,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                          @RequestParam(required = false) String deviceId,
                          @RequestParam(required = false) String topic,
                          @RequestParam(required = false) String dataType,
                          @RequestParam(required = false) Double minValue,
                          @RequestParam(required = false) Double maxValue) throws Exception {
        List<IotMessageDto> result = service.searchMessages(startDate, endDate, deviceId, topic, dataType, minValue, maxValue, 1, 1000, "DESC");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=iot_messages.csv");
        PrintWriter writer = response.getWriter();
        writer.println("received_at,device_id,topic,data_type,value,raw_message");
        for (IotMessageDto msg : result) {
            writer.printf("%s,%s,%s,%s,%s,%s\n",
                    msg.getReceivedAt(), msg.getDeviceId(), msg.getTopic(), msg.getDataType(), msg.getValue(), msg.getRawMessage());
        }
        writer.flush();
    }
}
