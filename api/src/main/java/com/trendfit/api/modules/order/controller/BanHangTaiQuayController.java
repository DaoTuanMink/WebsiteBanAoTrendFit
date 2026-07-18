package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.OrderRequestDTO;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pos-orders")
@CrossOrigin("*")
public class BanHangTaiQuayController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> taoDonBanTaiQuay(@RequestBody OrderRequestDTO dto) {
        try {
            DonHang donHang = orderService.taoDonHangTaiQuay(dto);
            return ResponseEntity.ok(donHang);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}