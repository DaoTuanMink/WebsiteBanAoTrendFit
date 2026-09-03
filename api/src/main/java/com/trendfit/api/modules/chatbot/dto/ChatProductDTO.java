package com.trendfit.api.modules.chatbot.dto;

import java.math.BigDecimal;
import java.util.List;

public record ChatProductDTO(
        Integer id,
        String name,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer discountPercent,
        String imageUrl,
        Integer stock,
        Long soldQuantity,
        String brand,
        String category,
        String material,
        String gender,
        List<String> sizes,
        List<String> colors,
        String detailUrl
) {
}
