package com.trendfit.api.modules.historyOrder.controller;

import com.trendfit.api.modules.historyOrder.dto.CustomerActivitySummaryDTO;
import com.trendfit.api.modules.historyOrder.dto.HistoryOrderDTO;
import com.trendfit.api.modules.historyOrder.service.HistoryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history-order")
@RequiredArgsConstructor
@CrossOrigin("*")
public class HistoryOrderController {

    private final HistoryOrderService historyOrderService;

    @GetMapping("/{userId}")
    public List<HistoryOrderDTO> getHistory(@PathVariable Integer userId) {
        return historyOrderService.getHistory(userId);
    }

    // Chức năng "lịch sử hoạt động bản thân" : hàng mua nhiều nhất, tổng tiền đã chi
    @GetMapping("/{userId}/summary")
    public CustomerActivitySummaryDTO getSummary(@PathVariable Integer userId) {
        return historyOrderService.getSummary(userId);
    }
}