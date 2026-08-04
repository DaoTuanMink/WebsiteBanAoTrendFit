package com.trendfit.api.modules.user.controller;

import com.trendfit.api.modules.user.dto.UserProfileDTO;
import com.trendfit.api.modules.user.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired private UserProfileService userProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userId, @RequestBody UserProfileDTO dto) {
        userProfileService.updateProfile(userId, dto);
        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @PostMapping("/{userId}/address")
    public ResponseEntity<?> saveAddress(@PathVariable Integer userId, @RequestBody UserProfileDTO.DiaChiDTO dcDto) {
        userProfileService.saveOrUpdateAddress(userId, dcDto);
        return ResponseEntity.ok("Lưu địa chỉ thành công!");
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer addressId) {
        userProfileService.deleteAddress(addressId);
        return ResponseEntity.ok("Xóa địa chỉ thành công!");
    }
}