package com.trendfit.api.modules.user.controller;

import com.trendfit.api.modules.user.dto.NhanVienDTO;
import com.trendfit.api.modules.user.repository.NhanVienRepository;
import com.trendfit.api.modules.user.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API quản lý tài khoản NHÂN VIÊN — CHỈ dành cho ADMIN.
 * Việc chặn quyền được thực hiện tự động bởi AuthInterceptor (áp dụng cho
 * toàn bộ "/api/admin/**", với "/api/admin/users" nằm trong nhóm ADMIN_ONLY).
 * Controller ở đây chỉ tập trung vào nghiệp vụ + trả lỗi rõ ràng cho FE.
 */
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin("*")
public class UserAdminController {
    @Autowired private UserAdminService userAdminService;
    @Autowired private NhanVienRepository nhanVienRepository;

    // Tạo tài khoản nhân viên mới (kèm tài khoản đăng nhập)
    @PostMapping("/create-staff")
    public ResponseEntity<?> createStaff(@RequestBody NhanVienDTO dto) {
        try {
            userAdminService.taoTaiKhoanNhanVien(dto);
            return ResponseEntity.ok("Tạo nhân viên thành công!");
        } catch (IllegalArgumentException e) {
            // Lỗi do dữ liệu không hợp lệ / trùng lặp -> 409 Conflict, kèm
            // thông báo tiếng Việt để FE hiển thị trực tiếp cho người dùng.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // Tìm kiếm theo mã nhân viên (dùng cho ô tìm kiếm ở trang quản lý nhân viên)
    @GetMapping("/search")
    public ResponseEntity<?> searchStaff(@RequestParam String maNhanVien) {
        return ResponseEntity.ok(nhanVienRepository.findByMaNhanVienContaining(maNhanVien));
    }

    // Sửa thông tin nhân viên (không đổi email/mật khẩu ở API này)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Integer id, @RequestBody NhanVienDTO dto) {
        try {
            userAdminService.capNhatNhanVien(id, dto);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Xóa nhân viên (đồng thời xóa luôn tài khoản đăng nhập liên kết)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Integer id) {
        try {
            userAdminService.xoaNhanVien(id);
            return ResponseEntity.ok("Đã xóa nhân viên!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Lấy toàn bộ danh sách nhân viên (bảng chính của trang quản lý)
    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        return ResponseEntity.ok(nhanVienRepository.findAll());
    }
}
