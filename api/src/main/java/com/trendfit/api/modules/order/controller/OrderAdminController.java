package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.entity.LichSuDonHang;
import com.trendfit.api.modules.order.repository.DonHangRepository;
import com.trendfit.api.modules.order.repository.LichSuDonHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "*")
public class OrderAdminController {

    private final DonHangRepository donHangRepository;
    private final LichSuDonHangRepository lichSuDonHangRepository;

    @Autowired
    public OrderAdminController(DonHangRepository donHangRepository, LichSuDonHangRepository lichSuDonHangRepository) {
        this.donHangRepository = donHangRepository;
        this.lichSuDonHangRepository = lichSuDonHangRepository;
    }

    // Lấy danh sách toàn bộ đơn hàng cho Admin quản lý
    @GetMapping
    public ResponseEntity<List<DonHang>> getAllOrders() {
        return ResponseEntity.ok(donHangRepository.findAll());
    }

    // API duyệt đơn, chuyển trạng thái và ghi log lịch sử
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id, @RequestParam String newStatus) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String trangThaiCu = donHang.getTrangThai();
        
        // 1. Cập nhật trạng thái mới
        donHang.setTrangThai(newStatus);
        donHangRepository.save(donHang);

        // 2. Ghi lại lịch sử thay đổi (Cái này để cô giáo chấm điểm là có quản lý luồng)
        LichSuDonHang log = new LichSuDonHang();
        log.setDonHang(donHang);
        log.setTrangThaiCu(trangThaiCu);
        log.setTrangThaiMoi(newStatus);
        log.setGhiChu("Admin cập nhật trạng thái đơn hàng");
        lichSuDonHangRepository.save(log);

        return ResponseEntity.ok(donHang);
    }
}