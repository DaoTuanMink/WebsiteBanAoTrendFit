package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin("*")
public class ProductAdminController {
    
    @Autowired private SanPhamService sanPhamService;
    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private ThuongHieuRepository thuongHieuRepository;

    @GetMapping("/metadata")
    public ResponseEntity<?> getMetadata() {
        return ResponseEntity.ok(sanPhamService.getMetadata());
    }

    @PostMapping("/full")
    public ResponseEntity<?> createFull(@RequestBody ProductSaveDTO dto) {
        return ResponseEntity.ok(sanPhamService.luuSanPhamFull(dto));
    }

    @GetMapping
public ResponseEntity<?> getAll() {
    return ResponseEntity.ok(sanPhamService.findAll());
}

@DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable Integer id) {
    sanPhamService.delete(id); // Bạn cần viết hàm delete này trong SanPhamService
    return ResponseEntity.ok("Đã xóa");
}

@PutMapping("/full")
public ResponseEntity<?> updateFull(@RequestBody ProductSaveDTO dto) {
    // Sửa sản phẩm chính và cập nhật danh sách biến thể/ảnh
    return ResponseEntity.ok(sanPhamService.capNhatSanPhamFull(dto));
}

// --- DANH MỤC ---
@GetMapping("/categories")
public ResponseEntity<?> getAllCategories() { return ResponseEntity.ok(danhMucRepository.findAll()); }

@PostMapping("/categories")
public ResponseEntity<?> saveCategory(@RequestBody DanhMuc dm) { return ResponseEntity.ok(danhMucRepository.save(dm)); }

@DeleteMapping("/categories/{id}")
public ResponseEntity<?> deleteCategory(@PathVariable Integer id) { 
    danhMucRepository.deleteById(id); 
    return ResponseEntity.ok("Xóa thành công"); 
}

// --- QUẢN LÝ THƯƠNG HIỆU ---
    @GetMapping("/brands")
    public ResponseEntity<?> getAllBrands() {
        return ResponseEntity.ok(thuongHieuRepository.findAll());
    }

    @PostMapping("/brands")
    public ResponseEntity<?> saveBrand(@RequestBody ThuongHieu thuongHieu) {
        return ResponseEntity.ok(thuongHieuRepository.save(thuongHieu));
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer id) {
        thuongHieuRepository.deleteById(id);
        return ResponseEntity.ok("Xóa thương hiệu thành công");
    }

    // Thêm API này vào Controller
@GetMapping("/{id}/variants")
public ResponseEntity<?> getVariants(@PathVariable Integer id) {
    return ResponseEntity.ok(sanPhamService.findBySanPhamId(id));
}
}