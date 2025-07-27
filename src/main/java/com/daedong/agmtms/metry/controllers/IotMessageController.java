
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
