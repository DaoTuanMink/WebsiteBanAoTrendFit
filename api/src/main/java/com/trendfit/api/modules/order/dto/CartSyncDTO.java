package com.trendfit.api.modules.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartSyncDTO {
    private Integer userId;
    private List<CartItemDTO> items;

    @Data
    public static class CartItemDTO {
        private Integer bienTheId;
        private Integer quantity;
    }
}