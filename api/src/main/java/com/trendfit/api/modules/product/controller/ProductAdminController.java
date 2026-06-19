package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import com.trendfit.api.modules.product.service.SanPhamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
public class ProductAdminController {

    private final SanPhamService sanPhamService;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    public ProductAdminController(
            SanPhamService sanPhamService,
            DanhMucRepository danhMucRepository,
            ThuongHieuRepository thuongHieuRepository) {
        this.sanPhamService = sanPhamService;
        this.danhMucRepository = danhMucRepository;
        this.thuongHieuRepository = thuongHieuRepository;
    }

    @GetMapping
    public List<SanPham> getAllProducts(@RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return sanPhamService.searchByTen(search);
        }
        return sanPhamService.getAll();
    }

    @GetMapping("/categories")
    public List<DanhMuc> getCategories() {
        return danhMucRepository.findByDangHoatDongTrueOrderByThuTuAsc();
    }

    @GetMapping("/brands")
    public List<ThuongHieu> getBrands() {
        return thuongHieuRepository.findByDangHoatDongTrueOrderByTenAsc();
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<?> getFullProduct(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(sanPhamService.getFullProduct(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/full")
    public ResponseEntity<?> createFullProduct(@RequestBody ProductSaveDTO dto) {
        if (dto.getSanPham() == null || dto.getSanPham().getTen() == null || dto.getSanPham().getTen().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Tên sản phẩm trống!"));
        }
        if (dto.getBienThes() == null || dto.getBienThes().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Phải có ít nhất 1 biến thể!"));
        }
        if (dto.getAnhs() == null || dto.getAnhs().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Phải có ít nhất 1 hình ảnh!"));
        }

        SanPham saved = sanPhamService.saveFullProduct(dto);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Đã thêm sản phẩm cùng biến thể và album ảnh thành công!",
                "data", saved));
    }

    @PutMapping("/{id}/full")
    public ResponseEntity<?> updateFullProduct(@PathVariable Integer id, @RequestBody ProductSaveDTO dto) {
        if (dto.getSanPham() == null || dto.getSanPham().getTen() == null || dto.getSanPham().getTen().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Tên sản phẩm trống!"));
        }
        if (dto.getBienThes() == null || dto.getBienThes().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Phải có ít nhất 1 biến thể!"));
        }
        if (dto.getAnhs() == null || dto.getAnhs().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thao tác thất bại: Phải có ít nhất 1 hình ảnh!"));
        }

        try {
            SanPham updated = sanPhamService.updateFullProduct(id, dto);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã cập nhật sản phẩm thành công!",
                    "data", updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            sanPhamService.delete(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Đã xóa sản phẩm khỏi hệ thống thành công!"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Thất bại: Không thể xóa sản phẩm này do vướng ràng buộc dữ liệu hóa đơn/đơn hàng lịch sử!"));
        }
    }
}
