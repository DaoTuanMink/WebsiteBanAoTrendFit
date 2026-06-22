package com.trendfit.api.modules.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {
    private String hoTen;
    private String diaChi;
    private String sdt;
    private BigDecimal tongTien;
    private List<OrderItemDTO> items;
}