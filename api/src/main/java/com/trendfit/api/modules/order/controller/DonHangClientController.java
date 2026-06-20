package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.repository.DonHangRepository;
import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/orders")
@CrossOrigin(origins = "*")
public class DonHangClientController {

    @Autowired
    private DonHangRepository donHangRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody DonHang requestDonHang) {
        try {
            // Khởi tạo mã hóa đơn TrendFit ngẫu nhiên và thời gian tạo
            requestDonHang.setMaDonHang("TF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            requestDonHang.setNgayTao(LocalDateTime.now());
            requestDonHang.setTrangThai("CHO_XAC_NHAN"); // Ép chặt logic chốt luồng theo yêu cầu của giáo viên

            // Lưu đơn hàng tổng quan trước
            DonHang savedOrder = donHangRepository.save(requestDonHang);

            // Gắn liên kết hai chiều và lưu danh sách chi tiết các mặt hàng được mua
            List<ChiTietDonHang> items = requestDonHang.getChiTietDonHangs();
            if (items != null) {
                for (ChiTietDonHang item : items) {
                    item.setDonHang(savedOrder);
                    chiTietDonHangRepository.save(item);
                }
            }
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi lưu hóa đơn: " + e.getMessage());
        }
    }
}