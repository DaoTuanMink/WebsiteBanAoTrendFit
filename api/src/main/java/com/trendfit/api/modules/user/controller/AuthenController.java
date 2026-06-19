package com.trendfit.api.modules.user.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Cho phép Frontend kết nối không bị chặn CORS
public class AuthenController {

    // API Đăng nhập xử lý phân quyền cho 3 Actor
    // Endpoint: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        // Luồng kiểm tra tài khoản thực tế (kiểm tra tài khoản mồi để chạy thử nhanh)
        if ("admin".equals(username) && "123".equals(password)) {
            response.put("status", "success");
            response.put("username", "Quản lý cấp cao");
            response.put("vaiTro", "ADMIN"); 
            response.put("token", "fake-jwt-token-for-admin-secret-key-2026");
        } else if ("nhanvien".equals(username) && "123".equals(password)) {
            response.put("status", "success");
            response.put("username", "Nhân viên điều hành");
            response.put("vaiTro", "EMPLOYEE"); 
            response.put("token", "fake-jwt-token-for-employee-secret-key-2026");
        } else if ("khachhang".equals(username) && "123".equals(password)) {
            response.put("status", "success");
            response.put("username", "Khách hàng TrendFit");
            response.put("vaiTro", "CUSTOMER"); 
            response.put("token", "fake-jwt-token-for-customer-secret-key-2026");
        } else {
            throw new RuntimeException("Tài khoản hoặc mật khẩu hệ thống không chính xác!");
        }
        
        return response;
    }
}