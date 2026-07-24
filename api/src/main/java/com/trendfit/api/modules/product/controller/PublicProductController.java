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

    // Đường dẫn thực tế: GET http://localhost:8080/api/public/products
    @GetMapping
    public ResponseEntity<?> getAllPublic() {
        return ResponseEntity.ok(sanPhamService.getAllPublicProducts());
    }

    // Đường dẫn thực tế: GET http://localhost:8080/api/public/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(sanPhamService.findByIdFull(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/public/products")
public ResponseEntity<?> getAllProducts() {
    // Gọi phương thức mới thay vì findAll() cũ
    return ResponseEntity.ok(sanPhamService.getAllPublicProducts());
}
}