package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
public class ProductAdminController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<List<SanPham>> getAllProducts(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "danhMucId", required = false) Long danhMucId,
            @RequestParam(value = "thuongHieuId", required = false) Long thuongHieuId) {
        
        List<SanPham> products = sanPhamService.timKiemSanPhamAdmin(search, danhMucId, thuongHieuId);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<SanPham> createProduct(@RequestBody SanPham sanPham) {
        SanPham savedProduct = sanPhamService.themMoiSanPhm(sanPham);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPham> updateProduct(@PathVariable Long id, @RequestBody SanPham updateData) {
        SanPham updatedProduct = sanPhamService.capNhatSanPham(id, updateData);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        sanPhamService.xoaSanPham(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công khỏi hệ thống TrendFit");
    }
}