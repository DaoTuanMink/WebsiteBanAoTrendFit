package com.trendfit.api.modules.chatbot.dto;

import java.util.List;

public record ChatRequest(
        String message,
        List<ChatHistoryDTO> history
) {
}