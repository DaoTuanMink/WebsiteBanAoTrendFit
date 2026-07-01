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
    public ResponseEntity<?> taoDonBanTaiQuay(
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestBody OrderRequestDTO dto
    ) {
        if (!"ADMIN".equals(role) && !"EMPLOYEE".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền bán hàng tại quầy");
        }

        DonHang donHang = orderService.taoDonHangTaiQuay(dto);
        return ResponseEntity.ok(donHang);
    }
}