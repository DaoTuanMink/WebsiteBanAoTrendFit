package com.trendfit.api.modules.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartSyncDTO {
    private Integer userId; // Phải khớp chữ 'userId'
    private List<CartItemDTO> items; // Phải khớp chữ 'items'

    @Data
    public static class CartItemDTO {
        private Integer bienTheId; // Phải khớp chữ 'bienTheId'
        private Integer quantity;  // Phải khớp chữ 'quantity'
    }
}