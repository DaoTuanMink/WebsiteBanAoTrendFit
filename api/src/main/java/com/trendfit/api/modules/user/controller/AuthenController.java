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

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập tài khoản và mật khẩu!");
        }

        // Alias demo (không phân biệt hoa thường) -> map sang email seed trong DB
        // admin / nhanvien / khachhang  (mật khẩu demo: 123)
        String lookupEmail = username.toLowerCase();
        if ("admin".equals(lookupEmail)) {
            lookupEmail = "admin";
        } else if ("nhanvien".equals(lookupEmail) || "employee".equals(lookupEmail) || "staff".equals(lookupEmail)) {
            lookupEmail = "nhanvien";
        } else if ("khachhang".equals(lookupEmail) || "customer".equals(lookupEmail) || "user".equals(lookupEmail)) {
            lookupEmail = "khachhang";
        }

        // 1) Ưu tiên tìm trong DB (DataInitializer seed admin/nhanvien/khachhang)
        NguoiDung user = nguoiDungRepository.findByEmail(lookupEmail);
        if (user == null && !lookupEmail.equals(username)) {
            // Thử đúng email người dùng gõ (đăng ký thật)
            user = nguoiDungRepository.findByEmail(username);
        }

        // 2) Fallback ADMIN demo nếu DB chưa seed (chấm điểm nhanh)
        if (user == null && "admin".equalsIgnoreCase(username) && "123".equals(password)) {
            return ResponseEntity.ok(createResponse(1, "Quản lý cấp cao", "ADMIN"));
        }

        if (user == null || user.getMatKhau() == null) {
            return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác! Dùng admin/nhanvien/khachhang - mật khẩu 123.");
        }

        if (Boolean.FALSE.equals(user.getDangHoatDong())) {
            return ResponseEntity.status(403).body("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        // Plain-text password (đồ án demo). Production nên dùng BCrypt.
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
