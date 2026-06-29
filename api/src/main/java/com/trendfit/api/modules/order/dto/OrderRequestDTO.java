package com.trendfit.api.modules.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OrderRequestDTO {
    // Thông tin giao hàng
    private String hoTen;
    private String sdt;
    private String diaChi;
    private String phuongThucThanhToan;
    
    // Thông tin tiền (BẮT BUỘC THÊM CÁC DÒNG NÀY)
    private BigDecimal tongTienHang;
    private BigDecimal tienGiam;
    private BigDecimal tongThanhToan;
    private Integer voucherId;

    @JsonProperty("userId") // Đảm bảo khớp với key trong JSON của Frontend
    private Integer userId;

    private Integer creatorId;
    
    private List<OrderItemDTO> items;

    
}