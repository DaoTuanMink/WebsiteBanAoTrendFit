package com.trendfit.api.modules.marketing.controller;

import com.trendfit.api.modules.marketing.dto.VoucherRequest;
import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/vouchers")
@CrossOrigin
public class PublicVoucherController {
    
    @Autowired 
    private MaGiamGiaService service;

    @PostMapping("/check")
    public MaGiamGia check(@RequestBody VoucherRequest request) {
        // Gọi service đã có để kiểm tra tính hợp lệ
        return service.kiemTraVoucher(request.getMa(), request.getTongDon());
    }
}