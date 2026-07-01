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
public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
    String username = loginRequest.get("username");
    String password = loginRequest.get("password");
    
    Map<String, Object> response = new HashMap<>();

    // 1. Kiểm tra tài khoản "cứng" (ADMIN, NHANVIEN)
    if ("admin".equals(username) && "123".equals(password)) {
        response.put("status", "success");
        response.put("id", 1);
        response.put("username", "Quản lý cấp cao");
        response.put("vaiTro", "ADMIN");
        response.put("token", "fake-jwt-token-admin");
        return response;
    } 
    
    if ("nhanvien".equals(username) && "123".equals(password)) {
        response.put("status", "success");
        response.put("id", 2);
        response.put("username", "Nhân viên điều hành");
        response.put("vaiTro", "EMPLOYEE");
        response.put("token", "fake-jwt-token-employee");
        return response;
    }

    // 2. Nếu không phải admin/nhân viên, kiểm tra trong Database
    NguoiDung user = nguoiDungRepository.findByEmail(username); // Giả sử username là email
    
    if (user != null && user.getMatKhau().equals(password)) {
        response.put("status", "success");
        response.put("id", user.getId());
        response.put("username", user.getHoTen());
        response.put("vaiTro", user.getVaiTro());
        response.put("token", "token-thuc-te-" + user.getId());
        return response;
    }

    // 3. Nếu không khớp cả 2 trường hợp trên
    throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!");
}
}