package com.trendfit.api.modules.chatbot.dto;

import java.util.List;

public record ChatResponse(
        String reply,
        List<ChatProductDTO> products
) {
}