package com.trendfit.api.modules.chatbot.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ChatService {

    
    private static final String INSTRUCTIONS = """
            Bạn là TrendFit AI, trợ lý mua sắm của cửa hàng thời trang TrendFit.
            Luôn trả lời bằng tiếng Việt, rõ ràng, thân thiện và ngắn gọn.
            Hỗ trợ khách về sản phẩm, kích cỡ, màu sắc, đặt hàng, giao hàng và đổi trả.
            Không tự bịa tồn kho, giá, khuyến mãi hoặc trạng thái đơn hàng. Khi cần dữ liệu
            đang thay đổi, hãy hướng dẫn khách kiểm tra trang sản phẩm hoặc mục Đơn hàng.
            Không yêu cầu khách cung cấp mật khẩu, API key hoặc thông tin thanh toán bí mật.
            """;

    private final WebClient openAiClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final long timeoutSeconds;

    public ChatService(
            ObjectMapper objectMapper,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.api.model:gpt-5.6-luna}") String model,
            @Value("${openai.api.timeout-seconds:60}") long timeoutSeconds) {
        this.openAiClient = WebClient.builder()
                .baseUrl(removeTrailingSlash(baseUrl))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
    }

    public String ask(String userMessage) {
        if (apiKey.isBlank()) {
            throw new ChatException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Backend chưa nhận được OPENAI_API_KEY. Hãy cấu hình biến môi trường rồi chạy lại API."
            );
        }

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("instructions", INSTRUCTIONS);
        requestBody.put("input", userMessage);
        requestBody.put("max_output_tokens", 600);
        requestBody.put("store", false);
        requestBody.putObject("reasoning").put("effort", "low");
        requestBody.putObject("text").put("verbosity", "low");

        try {
            JsonNode response = openAiClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            return extractReply(response);
        } catch (WebClientResponseException exception) {
            throw mapProviderError(exception);
        } catch (WebClientRequestException | IllegalStateException exception) {
            throw new ChatException(
                    HttpStatus.BAD_GATEWAY,
                    "Không thể kết nối tới dịch vụ AI. Hãy kiểm tra mạng và thử lại."
            );
        }
    }

    private String extractReply(JsonNode response) {
        if (response == null) {
            throw new ChatException(HttpStatus.BAD_GATEWAY, "Dịch vụ AI không trả về dữ liệu.");
        }

        StringBuilder reply = new StringBuilder();
        for (JsonNode outputItem : response.path("output")) {
            for (JsonNode contentItem : outputItem.path("content")) {
                if ("output_text".equals(contentItem.path("type").asText())) {
                    String text = contentItem.path("text").asText("").trim();
                    if (!text.isEmpty()) {
                        if (!reply.isEmpty()) {
                            reply.append(System.lineSeparator());
                        }
                        reply.append(text);
                    }
                }
            }
        }

        if (reply.isEmpty()) {
            throw new ChatException(
                    HttpStatus.BAD_GATEWAY,
                    "AI chưa tạo được câu trả lời. Vui lòng thử lại với câu hỏi khác."
            );
        }

        return reply.toString();
    }

    private ChatException mapProviderError(WebClientResponseException exception) {
        return switch (exception.getStatusCode().value()) {
            case 401, 403 -> new ChatException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "API key không hợp lệ hoặc chưa có quyền dùng model đã cấu hình."
            );
            case 429 -> new ChatException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Dịch vụ AI đang giới hạn lượt gọi hoặc tài khoản đã hết hạn mức. Vui lòng thử lại sau."
            );
            default -> new ChatException(
                    HttpStatus.BAD_GATEWAY,
                    "Dịch vụ AI đang gặp lỗi. Vui lòng thử lại sau."
            );
        };
    }

    private static String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://api.openai.com/v1";
        }
        return value.replaceFirst("/+$", "");
    }
}
