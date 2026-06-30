package com.trendfit.controller;

import com.trendfit.dto.ProductDTO;
import com.trendfit.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class ProductAdminController {
    @Autowired private ProductService productService;

    @PostMapping public ResponseEntity<?> create(@RequestBody ProductDTO dto) { return ResponseEntity.ok(productService.save(dto)); }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductDTO dto) { return ResponseEntity.ok(productService.update(id, dto)); }
    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) { productService.delete(id); return ResponseEntity.noContent().build(); }
}
