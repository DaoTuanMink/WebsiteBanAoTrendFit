package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.HoaDonChoRequestDTO;
import com.trendfit.api.modules.order.service.HoaDonChoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API "Hóa đơn chờ" (đơn tạm/treo giỏ hàng) cho màn hình "Bán hàng tại quầy".
 * Dùng chung được bởi ADMIN và EMPLOYEE (không nằm trong ADMIN_ONLY_PATHS
 * của AuthInterceptor) - vì đây là thao tác thu ngân hàng ngày, giống
 * BanHangTaiQuayController (/api/admin/pos-orders).
 */
@RestController
@RequestMapping("/api/admin/hoa-don-cho")
@CrossOrigin("*")
public class HoaDonChoController {

    @Autowired private HoaDonChoService hoaDonChoService;

    // Lưu tạm giỏ hàng hiện tại thành 1 hóa đơn chờ mới
    @PostMapping
    public ResponseEntity<?> luuTam(
            @RequestBody HoaDonChoRequestDTO dto,
            // Header do FE tự động gắn (xem utils/adminAuth.js) - biết "ai" vừa lưu tạm
            @RequestHeader(value = "NhanVien-ID", required = false) Integer nguoiTaoId) {
        try {
            return ResponseEntity.ok(hoaDonChoService.luuTam(dto, nguoiTaoId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy danh sách toàn bộ hóa đơn chờ hiện có
    @GetMapping
    public ResponseEntity<?> layDanhSach() {
        return ResponseEntity.ok(hoaDonChoService.layDanhSach());
    }

    // Lấy chi tiết 1 hóa đơn chờ (để "Gọi lại" khôi phục giỏ hàng)
    @GetMapping("/{id}")
    public ResponseEntity<?> layChiTiet(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(hoaDonChoService.layChiTiet(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Xóa 1 hóa đơn chờ (sau khi đã gọi lại xong, hoặc chủ động hủy)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoa(@PathVariable Integer id) {
        try {
            hoaDonChoService.xoa(id);
            return ResponseEntity.ok("Đã xóa hóa đơn chờ!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
