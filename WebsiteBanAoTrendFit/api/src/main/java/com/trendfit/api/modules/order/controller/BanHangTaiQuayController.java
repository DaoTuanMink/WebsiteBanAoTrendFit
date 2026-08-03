package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.OrderRequestDTO;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API tạo đơn hàng bán tại quầy (trang POS). Dùng chung được bởi ADMIN và
 * EMPLOYEE (không nằm trong ADMIN_ONLY_PATHS của AuthInterceptor) vì đây
 * chính là công việc thu ngân hàng ngày của nhân viên.
 */
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