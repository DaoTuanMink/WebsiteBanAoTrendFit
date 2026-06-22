package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/public/products") // Gom tất cả vào 1 đường dẫn duy nhất
@CrossOrigin(origins = "*")
public class SanPhamClientController {

    // @Autowired
    // private SanPhamService sanPhamService;

    // // 1. Lấy danh sách sản phẩm (Có search/filter)
    // @GetMapping
    // public ResponseEntity<List<SanPham>> getProducts(
    //         @RequestParam(value = "search", required = false, defaultValue = "") String search) {
    //     return ResponseEntity.ok(sanPhamService.timKiemSanPhamAdmin(search, null, null));
    // }

    // // 2. Lấy chi tiết sản phẩm
    // @GetMapping("/{id}")
    // public ResponseEntity<SanPham> getProductDetail(@PathVariable Integer id) {
    //     SanPham sanPham = sanPhamService.layChiTietSanPham(id);
    //     return (sanPham != null) ? ResponseEntity.ok(sanPham) : ResponseEntity.notFound().build();
    // }

    // // 3. Lấy biến thể (Size/Màu) - FIX LỖI Ở ĐÂY
    // @GetMapping("/{id}/variants")
    // public ResponseEntity<List<BienTheSanPham>> getProductVariants(@PathVariable Integer id) {
    //     return ResponseEntity.ok(sanPhamService.layBienTheCuaSanPham(id));
    // }
}