package com.trendfit.api.modules.marketing.controller;

import com.trendfit.api.modules.marketing.dto.VoucherRequest;
import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/vouchers")
@CrossOrigin
public class PublicVoucherController {

    @Autowired
    private MaGiamGiaService service;

    @PostMapping("/check")
    public ResponseEntity<?> check(@RequestBody VoucherRequest request) {
        try {
            MaGiamGia voucher = service.kiemTraVoucher(request.getMa(), request.getTongDon());
            return ResponseEntity.ok(voucher);
        } catch (RuntimeException e) {
            // TRƯỚC ĐÂY: không bắt exception nào -> mọi lỗi hợp lệ ("Mã hết
            // hạn", "Mã đã bị khóa"...) đều rơi thành lỗi 500 chung, khiến FE
            // (CheckoutView.vue) không đọc được thông báo thật, luôn hiển thị
            // "Mã không hợp lệ!" dù lý do thực sự là gì. Giờ trả 400 kèm
            // đúng nội dung lỗi để FE hiển thị chính xác cho người dùng.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
