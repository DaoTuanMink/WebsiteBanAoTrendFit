package com.trendfit.api.modules.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Integer bienTheId;
    private Integer quantity;
    private String ten;
    private BigDecimal gia;
}