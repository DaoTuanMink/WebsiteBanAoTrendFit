package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/products")
@CrossOrigin(origins = "*")
public class SanPhamClientController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<List<SanPham>> getProducts(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "danhMucId", required = false) Integer danhMucId,
            @RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId) {
        return ResponseEntity.ok(sanPhamService.timKiemSanPhamAdmin(search, danhMucId, thuongHieuId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPham> getProductDetail(@PathVariable Integer id) {
        SanPham sanPham = sanPhamService.layChiTietSanPham(id);
        if (sanPham == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sanPham);
    }
}