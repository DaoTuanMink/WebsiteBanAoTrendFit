package com.trendfit.api.modules.interaction.controller;

import com.trendfit.api.modules.interaction.dto.DanhGiaRequestDTO;
import com.trendfit.api.modules.interaction.dto.DanhGiaResponseDTO;
import com.trendfit.api.modules.interaction.service.DanhGiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/reviews")
@CrossOrigin(origins = "*")
public class PublicDanhGiaController {

    private final DanhGiaService danhGiaService;

    public PublicDanhGiaController(DanhGiaService danhGiaService) {
        this.danhGiaService = danhGiaService;
    }

    @GetMapping("/product/{sanPhamId}")
    public List<DanhGiaResponseDTO> layDanhGiaTheoSanPham(@PathVariable Integer sanPhamId) {
        return danhGiaService.layDanhGiaTheoSanPham(sanPhamId);
    }

    @GetMapping("/can-review")
    public boolean coTheDanhGia(
            @RequestParam Integer nguoiDungId,
            @RequestParam Integer sanPhamId
    ) {
        return danhGiaService.coTheDanhGia(nguoiDungId, sanPhamId);
    }

    @PostMapping
    public ResponseEntity<?> taoDanhGia(@RequestBody DanhGiaRequestDTO request) {
        try {
            return ResponseEntity.ok(danhGiaService.taoDanhGia(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}