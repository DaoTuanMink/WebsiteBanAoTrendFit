package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.entity.*;
import com.trendfit.api.modules.product.repository.*;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin("*")
public class ProductAdminController {
    @Autowired private SanPhamService sanPhamService;
    @Autowired private BienTheSanPhamRepository bienTheSanPhamRepository;
    @Autowired private AnhSanPhamRepository anhSanPhamRepository;
    @Autowired private DanhMucRepository danhMucRepository;
@Autowired private ThuongHieuRepository thuongHieuRepository;

@GetMapping("/metadata")
public ResponseEntity<?> getMetadata() {
    Map<String, Object> data = new HashMap<>();
    data.put("danhMucs", danhMucRepository.findAll());
    data.put("thuongHieus", thuongHieuRepository.findAll());
    return ResponseEntity.ok(data);

    // Lấy biến thể của 1 sản phẩm
    @GetMapping("/{id}/variants")
    public List<BienTheSanPham> getVariants(@PathVariable Integer id) {
        return bienTheSanPhamRepository.findBySanPham_Id(id);
    }

    // Lưu biến thể (Chỉ cập nhật danh sách biến thể)
    @PostMapping("/{id}/variants")
    public ResponseEntity<?> saveVariants(@PathVariable Integer id, @RequestBody List<BienTheSanPham> variants) {
        // Xóa cũ thêm mới để đảm bảo tính toàn vẹn
        bienTheSanPhamRepository.deleteBySanPham_Id(id);
        
        SanPham sp = new SanPham(); sp.setId(id);
        for(BienTheSanPham bt : variants) {
            bt.setSanPham(sp);
            bt.setId(null); // Đảm bảo tạo mới
        }
        return ResponseEntity.ok(bienTheSanPhamRepository.saveAll(variants));
    }
    
    // Tương tự cho Ảnh sản phẩm...
}

// @RestController
// @RequestMapping("/api/admin/products")
// @CrossOrigin(origins = "*")
// public class ProductAdminController {

//     @Autowired
//     private SanPhamService sanPhamService;

//     @GetMapping
//     public ResponseEntity<List<SanPham>> getAllProducts(
//             @RequestParam(value = "search", required = false, defaultValue = "") String search,
//             @RequestParam(value = "danhMucId", required = false) Integer danhMucId,
//             @RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId) {
        
//         List<SanPham> products = sanPhamService.timKiemSanPhamAdmin(search, danhMucId, thuongHieuId);
//         return ResponseEntity.ok(products);
//     }

//     @PostMapping
//     public ResponseEntity<SanPham> createProduct(@RequestBody SanPham sanPham) {
//         SanPham savedProduct = sanPhamService.themMoiSanPham(sanPham);
//         return ResponseEntity.ok(savedProduct);
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<SanPham> updateProduct(@PathVariable Integer id, @RequestBody SanPham updateData) {
//         SanPham updatedProduct = sanPhamService.capNhatSanPham(id, updateData);
//         return ResponseEntity.ok(updatedProduct);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<String> deleteProduct(@PathVariable Integer id) {
//         sanPhamService.xoaSanPham(id);
//         return ResponseEntity.ok("Xóa sản phẩm TrendFit thành công!");
//     }

//     @Autowired
//     private BienTheSanPhamService bienTheSanPhamService;

//     // Sửa @PathVariable từ Long sang Integer
//     @GetMapping("/{productId}/variants")
//     public ResponseEntity<?> getVariants(@PathVariable Integer productId) {
//         return ResponseEntity.ok(bienTheSanPhamService.getBySanPhamId(productId));
//     }

//     // Sửa @PathVariable từ Long sang Integer
//     @PostMapping("/{productId}/variants")
//     public ResponseEntity<?> saveVariants(@PathVariable Integer productId, @RequestBody List<BienTheSanPham> variants) {
//         return ResponseEntity.ok(bienTheSanPhamService.saveAll(productId, variants));
//     }
// }
