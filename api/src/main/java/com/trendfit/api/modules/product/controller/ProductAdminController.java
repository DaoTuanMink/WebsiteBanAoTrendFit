package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.KichCo;
import com.trendfit.api.modules.product.entity.MauSac;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.AnhSanPhamRepository;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.KichCoRepository;
import com.trendfit.api.modules.product.repository.MauSacRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin("*")
public class ProductAdminController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private AnhSanPhamRepository anhSanPhamRepository;

    @Autowired
    private KichCoRepository kichCoRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    // ==================== METADATA ====================
    @GetMapping("/metadata")
    public ResponseEntity<?> getMetadata() {
        return ResponseEntity.ok(sanPhamService.getMetadata());
    }

    // ==================== SẢN PHẨM ====================
    @PostMapping("/full")
    public ResponseEntity<?> createFull(@RequestBody ProductSaveDTO dto) {
        return ResponseEntity.ok(sanPhamService.luuSanPhamFull(dto));
    }

    @PutMapping("/full")
    public ResponseEntity<?> updateFull(@RequestBody ProductSaveDTO dto) {
        return ResponseEntity.ok(sanPhamService.capNhatSanPhamFull(dto));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(sanPhamService.findAllFull()); // Nên dùng Full để có biến thể + ảnh
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(sanPhamService.findByIdFull(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        sanPhamService.delete(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }

    // ==================== BIẾN THỂ & ẢNH ====================
    @GetMapping("/{id}/variants")
public ResponseEntity<?> getVariants(@PathVariable Integer id) {
    return ResponseEntity.ok(sanPhamService.findBySanPhamId(id));
}

    @GetMapping("/{id}/images")
    public ResponseEntity<?> getImages(@PathVariable Integer id) {
        return ResponseEntity.ok(anhSanPhamRepository.findBySanPham_Id(id));
    }

    // ==================== DANH MỤC ====================
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(danhMucRepository.findAll());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> saveCategory(@RequestBody DanhMuc dm) {
        return ResponseEntity.ok(danhMucRepository.save(dm));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        danhMucRepository.deleteById(id);
        return ResponseEntity.ok("Xóa danh mục thành công");
    }

    // ==================== THƯƠNG HIỆU ====================
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

    // ==================== QUẢN LÝ KÍCH CỠ ====================
@GetMapping("/sizes")
public ResponseEntity<?> getAllSizes() {
    return ResponseEntity.ok(kichCoRepository.findAll());
}

@PostMapping("/sizes")
public ResponseEntity<?> saveSize(@RequestBody KichCo kichCo) {
    return ResponseEntity.ok(kichCoRepository.save(kichCo));
}

@DeleteMapping("/sizes/{id}")
public ResponseEntity<?> deleteSize(@PathVariable Integer id) {
    kichCoRepository.deleteById(id);
    return ResponseEntity.ok("Xóa kích cỡ thành công");
}

// ==================== QUẢN LÝ MÀU SẮC ====================
@GetMapping("/colors")
public ResponseEntity<?> getAllColors() {
    return ResponseEntity.ok(mauSacRepository.findAll());
}

@PostMapping("/colors")
public ResponseEntity<?> saveColor(@RequestBody MauSac mauSac) {
    return ResponseEntity.ok(mauSacRepository.save(mauSac));
}

@DeleteMapping("/colors/{id}")
public ResponseEntity<?> deleteColor(@PathVariable Integer id) {
    mauSacRepository.deleteById(id);
    return ResponseEntity.ok("Xóa màu sắc thành công");
}
}