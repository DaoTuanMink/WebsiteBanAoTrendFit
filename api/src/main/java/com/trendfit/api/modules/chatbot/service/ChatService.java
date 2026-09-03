package com.trendfit.api.modules.chatbot.service;

import com.trendfit.api.modules.chatbot.dto.ChatHistoryDTO;
import com.trendfit.api.modules.chatbot.dto.ChatProductDTO;
import com.trendfit.api.modules.chatbot.dto.ChatResponse;
import com.trendfit.api.modules.chatbot.service.ChatProductService.SearchRequest;
import com.trendfit.api.modules.chatbot.service.ChatProductService.SearchResult;
import com.trendfit.api.modules.chatbot.service.ChatProductService.SortMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final String INSTRUCTIONS = """
            Bạn là TrendFit AI, trợ lý mua sắm của cửa hàng thời trang TrendFit.
            Luôn trả lời bằng tiếng Việt, ngắn gọn, thân thiện và đúng trọng tâm.
            Không tự bịa dữ liệu sản phẩm, chính sách cửa hàng hoặc thông tin cá nhân.
            Khi tư vấn size, hướng dẫn khách đối chiếu số đo với bảng size của sản phẩm;
            không nhận xét hình thể và không khẳng định size chỉ từ cân nặng.
            Không yêu cầu mật khẩu, API key hoặc thông tin thanh toán bí mật.
            """;

    private static final String MONEY =
            "(\\d+(?:[.,]\\d+)*)\\s*(trieu|nghin|ngan|k|vnd|dong|d)?";
    private static final Pattern PRICE_RANGE = Pattern.compile(
            "(?:tu\\s+)?" + MONEY + "\\s*(?:den|toi|-)\\s*" + MONEY
    );
    private static final Pattern PRICE_MAX = Pattern.compile(
            "(?:duoi|khong qua|toi da|nho hon|be hon)\\s*" + MONEY
                    + "|" + MONEY + "\\s*(?:tro xuong|do lai)"
    );
    private static final Pattern PRICE_MIN = Pattern.compile(
            "(?:tren|hon|lon hon|cao hon|it nhat|toi thieu|tu)\\s*" + MONEY
                    + "|" + MONEY + "\\s*(?:tro len)"
    );
    private static final Pattern PRICE_AROUND = Pattern.compile(
            "(?:khoang|tam|gan|gia)\\s*(?:la\\s*)?" + MONEY
    );
    private static final Pattern SIZE = Pattern.compile(
            "(?:size|kich co|co)\\s*[:=\\-]?\\s*(xxxl|xxl|xl|xs|s|m|l)(?:\\b|$)"
    );

    private static final List<String> PRODUCT_KEYWORDS = List.of(
            "ao thun basic essential", "ao thun graphic street",
            "ao so mi linen summer", "ao polo premium pique",
            "ao thun relax fit", "basic essential", "graphic street",
            "linen summer", "premium pique", "relax fit", "essential",
            "graphic", "premium", "basic", "linen", "pique", "relax",
            "ao so mi", "ao phong", "ao thun", "ao polo", "ao khoac",
            "quan jeans", "quan jean", "quan", "vay", "dam", "polo", "ao"
    );
    private static final List<String> COLORS = List.of(
            "xanh navy", "xanh duong", "xanh la", "trang", "den", "xam",
            "be", "do", "hong", "vang", "nau", "tim", "cam", "xanh"
    );
    private static final List<String> MATERIALS = List.of(
            "cotton", "linen", "denim", "kaki", "polyester", "ni", "len",
            "lua", "modal", "viscose"
    );
    private static final Set<String> PRODUCT_CUES = Set.of(
            "san pham", "mua do", "mua ao", "mua quan", "mua vay", "mua dam", "tim ao",
            "tim quan", "con hang", "gia", "size", "mau", "thuong hieu",
            "chat lieu", "nam", "nu", "unisex", "ban chay", "noi bat",
            "re nhat", "dat nhat", "giam gia", "sale", "khuyen mai", "so sanh"
    );

    private final WebClient openAiClient;
    private final ObjectMapper objectMapper;
    private final ChatProductService chatProductService;
    private final String apiKey;
    private final String model;
    private final long timeoutSeconds;

    public ChatService(
            ObjectMapper objectMapper,
            ChatProductService chatProductService,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.api.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.api.model:gpt-5.6-luna}") String model,
            @Value("${openai.api.timeout-seconds:60}") long timeoutSeconds
    ) {
        this.openAiClient = WebClient.builder()
                .baseUrl(removeTrailingSlash(baseUrl)).build();
        this.objectMapper = objectMapper;
        this.chatProductService = chatProductService;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
    }

    public ChatResponse ask(String userMessage, List<ChatHistoryDTO> history) {
        List<String> context = currentSearchContext(userMessage, history);
        String combined = String.join(" ", context);
        String normalizedCurrent = normalizeText(userMessage);

        if (isComparison(normalizedCurrent)) {
            List<ChatProductDTO> products = chatProductService.compareProducts(combined, 3);
            if (products.size() >= 2) {
                return new ChatResponse(comparisonReply(products), products);
            }
            return new ChatResponse(
                    "Bạn hãy ghi rõ tên 2 sản phẩm muốn so sánh, ví dụ: “So sánh áo Basic Essential và Relax Fit”.",
                    List.of()
            );
        }

        SearchState state = new SearchState();
        for (String item : context) {
            applyMessage(state, item);
        }

        if (isSizeAdvice(normalizedCurrent) && !containsProductKeyword(normalizedCurrent)) {
            return new ChatResponse(
                    "Để chọn size chính xác, bạn hãy đo vòng ngực, vai và chiều dài áo rồi đối chiếu bảng size trên trang sản phẩm. Nếu gửi tên sản phẩm cùng các số đo này, mình sẽ giúp bạn đối chiếu.",
                    List.of()
            );
        }

        if (state.productIntent || isProductQuestion(normalizedCurrent)) {
            SearchRequest request = new SearchRequest(
                    combined, state.keyword, state.brand, state.category,
                    state.material, state.gender, state.size, state.color,
                    state.minPrice, state.maxPrice, state.saleOnly, state.sortMode
            );
            SearchResult result = chatProductService.search(request, 3);
            return new ChatResponse(productReply(state, result), result.products());
        }

        return askGeneralQuestion(userMessage, history);
    }

    public ChatResponse ask(String userMessage) {
        return ask(userMessage, List.of());
    }

    private List<String> currentSearchContext(
            String currentMessage,
            List<ChatHistoryDTO> history
    ) {
        List<String> userMessages = new ArrayList<>();
        if (history != null) {
            history.stream()
                    .filter(item -> item != null && "user".equalsIgnoreCase(item.role()))
                    .map(ChatHistoryDTO::content)
                    .filter(value -> value != null && !value.isBlank())
                    .skip(Math.max(0, history.size() - 8L))
                    .forEach(userMessages::add);
        }
        userMessages.add(currentMessage);

        int start = 0;
        for (int index = 0; index < userMessages.size(); index++) {
            if (startsNewSearch(normalizeText(userMessages.get(index)))) {
                start = index;
            }
        }
        return new ArrayList<>(userMessages.subList(start, userMessages.size()));
    }

    private boolean startsNewSearch(String message) {
        boolean startsFresh = message.matches(
                ".*\\b(toi muon|muon mua|tim|cho (toi )?xem|co .* nao|san pham nao).*"
        );
        boolean namesNewTarget = containsProductKeyword(message)
                || message.contains("san pham") || message.contains("mua do")
                || message.contains("mua quan") || message.contains("mua vay")
                || message.contains("mua dam");
        return startsFresh && namesNewTarget;
    }

    private void applyMessage(SearchState state, String rawMessage) {
        String message = normalizeText(rawMessage);
        state.productIntent |= isProductQuestion(message) || containsProductKeyword(message);

        String keyword = firstContained(message, PRODUCT_KEYWORDS);
        if (!keyword.isBlank()) state.keyword = keyword;

        if (containsWord(message, "nam")) {
            state.gender = "nam";
        } else if (containsWord(message, "nu")) {
            state.gender = "nu";
        } else if (containsWord(message, "unisex")) {
            state.gender = "unisex";
        }

        Matcher size = SIZE.matcher(message);
        if (size.find()) state.size = size.group(1).toUpperCase(Locale.ROOT);

        String color = firstContainedWord(message, COLORS);
        if (!color.isBlank()) state.color = color;
        String material = firstContainedWord(message, MATERIALS);
        if (!material.isBlank()) {
            state.material = material;
            state.productIntent = true;
        }

        PriceBounds prices = extractPriceBounds(message);
        if (prices.detected) {
            state.minPrice = prices.min;
            state.maxPrice = prices.max;
        }

        if (message.contains("re nhat") || message.contains("gia thap nhat")) {
            state.sortMode = SortMode.CHEAPEST;
        } else if (message.contains("dat nhat") || message.contains("gia cao nhat")) {
            state.sortMode = SortMode.MOST_EXPENSIVE;
        } else if (message.contains("ban chay") || message.contains("mua nhieu")) {
            state.sortMode = SortMode.BEST_SELLING;
        } else if (message.contains("noi bat") || containsWord(message, "hot")) {
            state.sortMode = SortMode.FEATURED;
        }
        if (message.contains("giam gia") || message.contains("khuyen mai")
                || containsWord(message, "sale")) {
            state.saleOnly = true;
            state.sortMode = SortMode.DISCOUNT;
        }
    }

    private PriceBounds extractPriceBounds(String message) {
        Matcher range = PRICE_RANGE.matcher(message);
        if (range.find()) {
            return orderedBounds(parseMoney(range.group(1), range.group(2)),
                    parseMoney(range.group(3), range.group(4)));
        }
        Matcher maximum = PRICE_MAX.matcher(message);
        if (maximum.find()) {
            int offset = maximum.group(1) != null ? 1 : 3;
            return new PriceBounds(null,
                    parseMoney(maximum.group(offset), maximum.group(offset + 1)), true);
        }
        Matcher minimum = PRICE_MIN.matcher(message);
        if (minimum.find()) {
            int offset = minimum.group(1) != null ? 1 : 3;
            return new PriceBounds(
                    parseMoney(minimum.group(offset), minimum.group(offset + 1)), null, true);
        }
        Matcher around = PRICE_AROUND.matcher(message);
        if (around.find()) {
            BigDecimal value = parseMoney(around.group(1), around.group(2));
            if (value != null) {
                BigDecimal margin = value.multiply(new BigDecimal("0.20"));
                return new PriceBounds(value.subtract(margin), value.add(margin), true);
            }
        }
        return new PriceBounds(null, null, false);
    }

    private PriceBounds orderedBounds(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) return new PriceBounds(first, second, true);
        return first.compareTo(second) <= 0
                ? new PriceBounds(first, second, true)
                : new PriceBounds(second, first, true);
    }

    private BigDecimal parseMoney(String number, String unit) {
        if (number == null || number.isBlank()) return null;
        String normalizedUnit = unit == null ? "" : unit;
        String compact = number.trim();
        BigDecimal value;
        if ("trieu".equals(normalizedUnit)) {
            value = new BigDecimal(compact.replace(',', '.'))
                    .multiply(BigDecimal.valueOf(1_000_000));
        } else if (Set.of("k", "nghin", "ngan").contains(normalizedUnit)) {
            value = new BigDecimal(compact.replace(',', '.'))
                    .multiply(BigDecimal.valueOf(1_000));
        } else {
            value = new BigDecimal(compact.replaceAll("[.,]", ""));
            if (normalizedUnit.isBlank() && value.compareTo(BigDecimal.valueOf(10_000)) < 0) {
                value = value.multiply(BigDecimal.valueOf(1_000));
            }
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String productReply(SearchState state, SearchResult result) {
        List<ChatProductDTO> products = result.products();
        if (products.isEmpty()) {
            return "Hiện chưa có sản phẩm đang bán, còn hàng và phù hợp với yêu cầu này. Bạn có thể thử đổi giá, size, màu hoặc loại sản phẩm.";
        }
        StringBuilder reply = new StringBuilder();
        if (!result.exactMatch()) {
            reply.append("Chưa có sản phẩm khớp toàn bộ yêu cầu. Các lựa chọn gần phù hợp nhất:\n");
        } else if (state.sortMode == SortMode.BEST_SELLING
                && products.stream().noneMatch(item -> safeLong(item.soldQuantity()) > 0)) {
            reply.append("Hiện chưa có dữ liệu lượt bán. Đây là các sản phẩm nổi bật đang còn hàng:\n");
        } else if (state.saleOnly) {
            reply.append("Các sản phẩm đang giảm giá phù hợp:\n");
        } else {
            reply.append("Mình tìm thấy các sản phẩm phù hợp:\n");
        }
        for (int index = 0; index < products.size(); index++) {
            ChatProductDTO product = products.get(index);
            reply.append(index + 1).append(". ").append(product.name())
                    .append(" – ").append(formatPrice(product.price()));
            if (safeInt(product.discountPercent()) > 0) {
                reply.append(" (giảm ").append(product.discountPercent()).append("%)");
            }
            reply.append("\n");
        }
        reply.append("Bạn có thể bấm vào thẻ sản phẩm bên dưới để xem chi tiết.");
        return reply.toString();
    }

    private String comparisonReply(List<ChatProductDTO> products) {
        StringBuilder reply = new StringBuilder("So sánh nhanh:\n");
        for (ChatProductDTO product : products) {
            reply.append("• ").append(product.name()).append(": ")
                    .append(formatPrice(product.price())).append(", còn ")
                    .append(product.stock()).append(" sản phẩm");
            if (product.sizes() != null && !product.sizes().isEmpty()) {
                reply.append(", size ").append(String.join(", ", product.sizes()));
            }
            if (product.colors() != null && !product.colors().isEmpty()) {
                reply.append(", màu ").append(String.join(", ", product.colors()));
            }
            reply.append(".\n");
        }
        reply.append("Bạn có thể mở từng thẻ bên dưới để xem đầy đủ thông tin.");
        return reply.toString();
    }

    private ChatResponse askGeneralQuestion(String message, List<ChatHistoryDTO> history) {
        if (apiKey.isBlank()) {
            return new ChatResponse(
                    "Mình có thể tìm sản phẩm theo tên, giá, size, màu, giới tính, chất liệu, thương hiệu; tìm hàng giảm giá, bán chạy hoặc so sánh sản phẩm. Với câu hỏi AI khác, backend cần cấu hình OPENAI_API_KEY.",
                    List.of()
            );
        }
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        request.put("instructions", INSTRUCTIONS);
        ArrayNode input = request.putArray("input");
        if (history != null) {
            history.stream().filter(item -> item != null && item.content() != null)
                    .skip(Math.max(0, history.size() - 8L))
                    .forEach(item -> input.addObject()
                            .put("role", "bot".equalsIgnoreCase(item.role()) ? "assistant" : "user")
                            .put("content", item.content()));
        }
        input.addObject().put("role", "user").put("content", message);
        request.put("max_output_tokens", 500);
        request.put("store", false);
        try {
            JsonNode response = openAiClient.post().uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(request)
                    .retrieve().bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
            return new ChatResponse(extractReply(response), List.of());
        } catch (WebClientResponseException exception) {
            throw mapProviderError(exception);
        } catch (WebClientRequestException | IllegalStateException exception) {
            throw new ChatException(HttpStatus.BAD_GATEWAY,
                    "Không thể kết nối tới dịch vụ AI. Hãy kiểm tra mạng.");
        }
    }

    private boolean isProductQuestion(String message) {
        return PRODUCT_CUES.stream().anyMatch(message::contains)
                || containsProductKeyword(message);
    }

    private boolean containsProductKeyword(String message) {
        return PRODUCT_KEYWORDS.stream().anyMatch(message::contains);
    }

    private boolean isComparison(String message) {
        return message.contains("so sanh") || message.contains("khac nhau");
    }

    private boolean isSizeAdvice(String message) {
        return message.contains("chon size") || message.contains("tu van size")
                || message.contains("mac size") || message.contains("size nao");
    }

    private String firstContained(String message, List<String> values) {
        return values.stream().filter(message::contains).findFirst().orElse("");
    }

    private String firstContainedWord(String message, List<String> values) {
        return values.stream().filter(value -> containsWord(message, value))
                .findFirst().orElse("");
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(word)
                + "([^a-z0-9]|$)").matcher(text).find();
    }

    private String normalizeText(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "chưa có giá";
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return format.format(price) + " đ";
    }

    private long safeLong(Long value) {
        return value == null ? 0 : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String extractReply(JsonNode response) {
        if (response == null) {
            throw new ChatException(HttpStatus.BAD_GATEWAY, "Dịch vụ AI không trả về dữ liệu.");
        }
        StringBuilder reply = new StringBuilder();
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    String text = content.path("text").asText("").trim();
                    if (!text.isBlank()) {
                        if (!reply.isEmpty()) reply.append(System.lineSeparator());
                        reply.append(text);
                    }
                }
            }
        }
        if (reply.isEmpty()) {
            throw new ChatException(HttpStatus.BAD_GATEWAY, "AI chưa tạo được câu trả lời.");
        }
        return reply.toString();
    }

    private ChatException mapProviderError(WebClientResponseException exception) {
        return switch (exception.getStatusCode().value()) {
            case 401, 403 -> new ChatException(HttpStatus.SERVICE_UNAVAILABLE,
                    "API key không hợp lệ hoặc không có quyền dùng model.");
            case 429 -> new ChatException(HttpStatus.TOO_MANY_REQUESTS,
                    "Dịch vụ AI đang giới hạn lượt gọi hoặc hết hạn mức.");
            default -> new ChatException(HttpStatus.BAD_GATEWAY,
                    "Dịch vụ AI đang gặp lỗi. Vui lòng thử lại sau.");
        };
    }

    private static String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) return "https://api.openai.com/v1";
        return value.replaceFirst("/+$", "");
    }

    private static final class SearchState {
        private String keyword = "";
        private String brand = "";
        private String category = "";
        private String material = "";
        private String gender = "";
        private String size = "";
        private String color = "";
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private boolean saleOnly;
        private SortMode sortMode = SortMode.DEFAULT;
        private boolean productIntent;
    }

    private record PriceBounds(BigDecimal min, BigDecimal max, boolean detected) {
    }
}
