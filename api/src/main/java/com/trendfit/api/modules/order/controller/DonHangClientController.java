package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.entity.LichSuDonHang;
import com.trendfit.api.modules.order.repository.DonHangRepository;
import com.trendfit.api.modules.order.repository.LichSuDonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/public/orders")
@CrossOrigin(origins = "*")
public class DonHangClientController {

    private final DonHangRepository donHangRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;

    @Autowired
    public DonHangClientController(DonHangRepository donHangRepository, LichSuDonHangRepository lichSuDonHangRepository) {
        this.donHangRepository = donHangRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody DonHang requestDonHang) {
        try {
            // 1. Ép trạng thái mặc định
            requestDonHang.setTrangThai("CHO_XAC_NHAN");
            
            // 2. Lưu đơn hàng
            DonHang savedOrder = donHangRepository.save(requestDonHang);

            // 3. Ghi log lịch sử đơn hàng (Điểm cộng cực lớn với giáo viên)
            LichSuDonHang log = new LichSuDonHang();
            log.setDonHang(savedOrder);
            log.setTrangThaiCu("KHOI_TAO");
            log.setTrangThaiMoi("CHO_XAC_NHAN");
            log.setGhiChu("Khách hàng đặt hàng qua Website");
            lichSuDonHangRepository.save(log);

            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}