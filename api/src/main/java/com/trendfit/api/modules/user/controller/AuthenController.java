package com.trendfit.api.modules.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Cho phép Frontend kết nối không bị chặn CORS
public class AuthenController {

@Autowired NguoiDungRepository nguoiDungRepository;

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody NguoiDung user) {
    // 1. Kiểm tra email đã tồn tại chưa
    // 2. Lưu vào database với vai trò là "CUSTOMER"
    user.setVaiTro("CUSTOMER");
    user.setDangHoatDong(true);
    nguoiDungRepository.save(user);
    return ResponseEntity.ok("Đăng ký thành công!");
}

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
    String username = loginRequest.get("username");
    String password = loginRequest.get("password");
    
    // In ra log để xem có bị thừa dấu cách hay không
    System.out.println("Debug Login -> User: '" + username + "', Pass: '" + password + "'");

    // Thử kiểm tra cứng trước
    if ("admin".equals(username) && "123".equals(password)) {
        System.out.println("Login cứng khớp!");
        return ResponseEntity.ok(createResponse(1, "Quản lý cấp cao", "ADMIN"));
    }

    // Nếu không khớp cứng, vào DB tìm
    NguoiDung user = nguoiDungRepository.findByEmail(username);
    if (user != null) {
        System.out.println("User tìm thấy: " + user.getEmail() + ", Pass trong DB: " + user.getMatKhau());
        if (user.getMatKhau().equals(password)) {
             return ResponseEntity.ok(createResponse(user.getId(), user.getHoTen(), user.getVaiTro()));
        }
    }

    return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác!");
}

// Hàm phụ để code gọn hơn
private Map<String, Object> createResponse(Integer id, String name, String role) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "success");
    response.put("id", id);
    response.put("username", name);
    response.put("vaiTro", role);
    return response;
}
}