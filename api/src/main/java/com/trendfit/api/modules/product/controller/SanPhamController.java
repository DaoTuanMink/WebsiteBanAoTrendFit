package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ngăn chặn hoàn toàn lỗi chặn CORS từ cổng Vue 3
public class SanPhamController {

    private final SanPhamService sanPhamService;

    // API 1: Lấy danh sách áo bán trên trang chủ
    // Link gọi thử: GET http://localhost:8080/api/products
    @GetMapping
    public List<SanPham> getAllProducts() {
        return sanPhamService.layTatCaSanPhamChoTrangChu();
    }

    // API 2: Lấy thông tin cơ bản của một sản phẩm
    // Link gọi thử: GET http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public SanPham getProductDetail(@PathVariable Integer id) {
        return sanPhamService.layChiTietSanPham(id);
    }

    // API 3: Lấy toàn bộ biến thể Size/Màu kèm giá của sản phẩm đó
    // Link gọi thử: GET http://localhost:8080/api/products/1/variants
    @GetMapping("/{id}/variants")
    public List<BienTheSanPham> getProductVariants(@PathVariable Integer id) {
        return sanPhamService.layBienTheCuaSanPham(id);
    }
}