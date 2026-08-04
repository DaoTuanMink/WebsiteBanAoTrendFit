package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.repository.DonHangRepository;
import com.trendfit.api.modules.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API quản trị Đơn hàng — ADMIN và EMPLOYEE đều dùng được (STAFF_AREA).
 * Nhân viên cần xem danh sách + duyệt/cập nhật trạng thái đơn online của khách.
 * AuthInterceptor không còn xếp path này vào ADMIN_ONLY_PATHS.
 */
@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin("*")
public class OrderAdminController {
    
    @Autowired 
    private OrderService orderService;
    @Autowired 
    private DonHangRepository donHangRepository;

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status,
            // Header do FE tự động gắn (xem main.js), chứa id của người đang đăng nhập
            // (nhân viên/admin) -> dùng để ghi lại "ai đã duyệt đơn hàng" (yêu cầu Thành)
            @RequestHeader(value = "NhanVien-ID", required = false) Integer nguoiThucHienId) {
        
        // Kiểm tra tính hợp lệ của trạng thái
        if (!status.matches("CHO_XAC_NHAN|DA_XAC_NHAN|DANG_VAN_CHUYEN|DA_THANH_CONG|DA_HUY")) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ");
        }
        
        orderService.capNhatTrangThaiDonHang(id, status, nguoiThucHienId);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }

@GetMapping
public ResponseEntity<?> getAllOrders() {
    return ResponseEntity.ok(orderService.findAllOrdersWithDetails());
}

// Lấy các đơn hàng chưa gán cho khách (userId is null)
@GetMapping("/null-user")
public ResponseEntity<?> getOrdersWithNullUser() {
    return ResponseEntity.ok(orderService.findOrdersWithNullUser());
}

// Xem lịch sử ai đã duyệt / đổi trạng thái của 1 đơn hàng (yêu cầu Thành)
@GetMapping("/{id}/history")
public ResponseEntity<?> getOrderHistory(@PathVariable Integer id) {
    return ResponseEntity.ok(orderService.getLichSuDonHang(id));
}

}