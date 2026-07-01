package com.trendfit.api.modules.marketing.controller;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vouchers")
@CrossOrigin
public class VoucherAdminController {

    @Autowired
    private MaGiamGiaService service;

    @GetMapping
    public List<MaGiamGia> getAll() {
        return service.getAll();
    }

    @PostMapping
    public MaGiamGia create(@RequestBody MaGiamGia mg) {
        return service.save(mg);
    }

    @PutMapping("/{id}")
    public MaGiamGia update(@PathVariable Integer id, @RequestBody MaGiamGia mg) {
        mg.setId(id);
        return service.save(mg);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    /*
     * NOTE DATN:
     * API này dùng cho bán hàng tại quầy.
     * Nhân viên nhập mã giảm giá, hệ thống kiểm tra:
     * - Mã có tồn tại không
     * - Mã còn hoạt động không
     * - Mã còn hạn không
     * - Đơn hàng có đạt giá trị tối thiểu không
     * - Mã còn lượt sử dụng không
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkVoucher(@RequestBody Map<String, Object> request) {
        String ma = request.get("ma") == null
                ? ""
                : request.get("ma").toString().trim();

        if (ma.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập mã giảm giá");
        }

        BigDecimal tongDon = BigDecimal.ZERO;

        if (request.get("tongDon") != null) {
            tongDon = new BigDecimal(request.get("tongDon").toString());
        }

        MaGiamGia voucher = service.getAll()
                .stream()
                .filter(item -> item.getMa() != null && item.getMa().equalsIgnoreCase(ma))
                .findFirst()
                .orElse(null);

        if (voucher == null) {
            return ResponseEntity.badRequest().body("Mã giảm giá không tồn tại");
        }

        if (voucher.getDangHoatDong() != null && !voucher.getDangHoatDong()) {
            return ResponseEntity.badRequest().body("Mã giảm giá đã bị khóa");
        }

        LocalDate today = LocalDate.now();

        if (voucher.getNgayBatDau() != null && today.isBefore(voucher.getNgayBatDau())) {
            return ResponseEntity.badRequest().body("Mã giảm giá chưa đến ngày sử dụng");
        }

        if (voucher.getNgayKetThuc() != null && today.isAfter(voucher.getNgayKetThuc())) {
            return ResponseEntity.badRequest().body("Mã giảm giá đã hết hạn");
        }

        BigDecimal donHangToiThieu = voucher.getDonHangToiThieu() == null
                ? BigDecimal.ZERO
                : voucher.getDonHangToiThieu();

        if (tongDon.compareTo(donHangToiThieu) < 0) {
            return ResponseEntity.badRequest().body("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã");
        }

        int gioiHan = voucher.getGioiHanSuDung() == null ? 0 : voucher.getGioiHanSuDung();
        int daDung = voucher.getSoLanDaDung() == null ? 0 : voucher.getSoLanDaDung();

        if (gioiHan > 0 && daDung >= gioiHan) {
            return ResponseEntity.badRequest().body("Mã giảm giá đã hết lượt sử dụng");
        }

        BigDecimal tienGiam = tinhTienGiam(voucher, tongDon);

        Map<String, Object> response = new HashMap<>();
        response.put("id", voucher.getId());
        response.put("ma", voucher.getMa());
        response.put("ten", voucher.getTen());
        response.put("loai", voucher.getLoai());
        response.put("giaTriGiam", voucher.getGiaTriGiam());
        response.put("giaTriToiDa", voucher.getGiaTriToiDa());
        response.put("donHangToiThieu", voucher.getDonHangToiThieu());
        response.put("tienGiam", tienGiam);

        return ResponseEntity.ok(response);
    }

    private BigDecimal tinhTienGiam(MaGiamGia voucher, BigDecimal tongDon) {
        BigDecimal giaTriGiam = voucher.getGiaTriGiam() == null
                ? BigDecimal.ZERO
                : voucher.getGiaTriGiam();

        String loai = voucher.getLoai() == null
                ? ""
                : voucher.getLoai().toUpperCase();

        BigDecimal tienGiam;

        if (
                "PERCENT".equals(loai)
                        || "PHAN_TRAM".equals(loai)
                        || "%".equals(loai)
        ) {
            tienGiam = tongDon
                    .multiply(giaTriGiam)
                    .divide(BigDecimal.valueOf(100));

            if (
                    voucher.getGiaTriToiDa() != null
                            && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0
            ) {
                tienGiam = voucher.getGiaTriToiDa();
            }
        } else {
            tienGiam = giaTriGiam;
        }

        if (tienGiam.compareTo(tongDon) > 0) {
            tienGiam = tongDon;
        }

        return tienGiam;
    }
}