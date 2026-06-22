package com.trendfit.api.modules.order.dto;

import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import lombok.Data;
import java.util.List;

@Data
public class OrderResponseDTO {
    private DonHang donHang;
    private List<ChiTietDonHang> chiTietDonHangs;
}