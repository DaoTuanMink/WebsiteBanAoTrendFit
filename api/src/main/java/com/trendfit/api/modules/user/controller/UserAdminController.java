package com.trendfit.api.modules.user.controller;

import com.trendfit.api.modules.user.dto.NhanVienDTO;
import com.trendfit.api.modules.user.repository.NhanVienRepository;
import com.trendfit.api.modules.user.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin("*")
public class UserAdminController {
    @Autowired private UserAdminService userAdminService;
    @Autowired private NhanVienRepository nhanVienRepository;

    @PostMapping("/create-staff")
    public ResponseEntity<?> createStaff(@RequestBody NhanVienDTO dto) {
        userAdminService.taoTaiKhoanNhanVien(dto);
        return ResponseEntity.ok("Tạo nhân viên thành công!");
    }

    @GetMapping("/staff")
public ResponseEntity<?> getAllStaff() {
    return ResponseEntity.ok(nhanVienRepository.findAll());
}
}