package com.trendfit.api.modules.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 *  ĐĂNG NHẬP / ĐĂNG KÝ DÙNG CHUNG CHO CẢ 3 LOẠI TÀI KHOẢN
 * ============================================================================
 * Hệ thống KHÔNG tách 3 API đăng nhập riêng cho Khách hàng / Nhân viên / Admin.
 * Tất cả cùng đăng nhập qua MỘT endpoint "/api/auth/login" bằng email + mật khẩu;
 * sự khác biệt nằm ở cột "vaiTro" (role) được trả về trong response:
 *
 *   - "CUSTOMER" : khách hàng tự đăng ký ở trang "/register" (xem register() bên dưới)
 *   - "EMPLOYEE" : do ADMIN tạo trong trang "Quản lý nhân viên" (xem UserAdminService)
 *   - "ADMIN"    : quản trị viên cấp cao (hiện có 1 tài khoản demo hard-code
 *                  "admin" / "123" bên dưới để tiện chấm điểm/demo đồ án -
 *                  KHÔNG dùng cách này khi triển khai thật, nên tạo tài khoản
 *                  ADMIN thật trong bảng nguoi_dung với vaiTro = 'ADMIN')
 *
 * Sau khi đăng nhập thành công, Frontend (xem LoginView.vue) lưu "vaiTro" vào
 * localStorage rồi:
 *   1. router/index.js dùng nó để quyết định cho vào khu vực /admin hay không.
 *   2. main.js dùng nó để tự động gắn header "User-Role" vào mọi request admin,
 *      để AuthInterceptor (backend) kiểm tra quyền trên từng API.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Cho phép Frontend kết nối không bị chặn CORS
public class AuthenController {

    @Autowired NguoiDungRepository nguoiDungRepository;

    /**
     * Đăng ký tài khoản KHÁCH HÀNG (public, ai cũng đăng ký được).
     * Tài khoản Nhân viên/Admin KHÔNG được tạo qua API này — phải do
     * ADMIN tạo thủ công trong trang "Quản lý nhân viên" để đảm bảo kiểm soát.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody NguoiDung user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập email!");
        }
        if (nguoiDungRepository.findByEmail(user.getEmail().trim()) != null) {
            return ResponseEntity.status(409).body("Email này đã được đăng ký!");
        }

        user.setEmail(user.getEmail().trim());
        user.setVaiTro("CUSTOMER"); // Luôn ép cứng vai trò CUSTOMER, không tin dữ liệu từ client
        user.setDangHoatDong(true);
        nguoiDungRepository.save(user);
        return ResponseEntity.ok("Đăng ký thành công!");
    }

    /**
     * Đăng nhập dùng chung cho Admin / Nhân viên / Khách hàng.
     * Body: { "username": "<email hoặc 'admin'>", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.getOrDefault("username", "").trim();
        String password = loginRequest.getOrDefault("password", "");

        // --- Tài khoản ADMIN demo, phục vụ chấm điểm/demo nhanh không cần seed DB ---
        if ("admin".equals(username) && "123".equals(password)) {
            return ResponseEntity.ok(createResponse(1, "Quản lý cấp cao", "ADMIN"));
        }

        // --- Tài khoản thật trong DB: dùng chung cho cả NHÂN VIÊN và KHÁCH HÀNG,
        //     vì cả hai đều là 1 bản ghi NguoiDung, chỉ khác giá trị "vaiTro" ---
        NguoiDung user = nguoiDungRepository.findByEmail(username);
        if (user == null || user.getMatKhau() == null) {
            return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác!");
        }

        // Tài khoản đã bị Admin khóa (dangHoatDong = false) -> không cho đăng nhập
        if (Boolean.FALSE.equals(user.getDangHoatDong())) {
            return ResponseEntity.status(403).body("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        // TODO (bảo mật): mật khẩu hiện lưu dạng chữ thường (plain text) nên so
        // sánh trực tiếp bằng equals(). Khi nâng cấp bảo mật, hãy đổi cột
        // mat_khau sang lưu dạng băm (BCrypt) và dùng passwordEncoder.matches(...)
        // ở đây thay vì equals() - đồng thời cập nhật lại chỗ tạo tài khoản
        // (AuthenController.register, UserAdminService.taoTaiKhoanNhanVien).
        if (!user.getMatKhau().equals(password)) {
            return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác!");
        }

        return ResponseEntity.ok(createResponse(user.getId(), user.getHoTen(), user.getVaiTro()));
    }

    // Đóng gói response trả về cho FE (id, tên hiển thị, vai trò)
    private Map<String, Object> createResponse(Integer id, String name, String role) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("id", id);
        response.put("username", name);
        response.put("vaiTro", role);
        return response;
    }
}
