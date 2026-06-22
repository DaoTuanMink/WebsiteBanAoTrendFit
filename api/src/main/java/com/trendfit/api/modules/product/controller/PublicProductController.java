package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.repository.SanPhamRepository;
import com.trendfit.api.modules.product.service.SanPhamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/products") // Đường dẫn này khớp với axios trong AoView.vue
@CrossOrigin("*")
public class PublicProductController {
    
    @Autowired 
    private SanPhamRepository sanPhamRepository;
    @Autowired private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<?> getAllPublic() {
        // Trả về toàn bộ danh sách sản phẩm cho khách hàng
        return ResponseEntity.ok(sanPhamRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer id) {
        try {
            // Bây giờ sanPhamService đã được tiêm vào, sẽ không còn lỗi null nữa
            return ResponseEntity.ok(sanPhamService.findByIdFull(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}