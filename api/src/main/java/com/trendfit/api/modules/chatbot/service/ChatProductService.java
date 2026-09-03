package com.trendfit.api.modules.chatbot.service;

import com.trendfit.api.modules.chatbot.dto.ChatProductDTO;
import com.trendfit.api.modules.product.entity.AnhSanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.repository.AnhSanPhamRepository;
import com.trendfit.api.modules.product.repository.BienTheSanPhamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
public class ChatProductService {

    private static final Set<String> COMPARISON_STOP_WORDS = Set.of(
            "ao", "thun", "so", "mi", "san", "pham", "va", "voi",
            "sanh", "giua", "loai", "mau", "size", "kich", "co",
            "nam", "nu", "unisex", "gia", "re", "dat", "nhat",
            "duoi", "tren", "tu", "den"
    );

    private final BienTheSanPhamRepository bienTheRepository;
    private final AnhSanPhamRepository anhRepository;

    public ChatProductService(
            BienTheSanPhamRepository bienTheRepository,
            AnhSanPhamRepository anhRepository
    ) {
        this.bienTheRepository = bienTheRepository;
        this.anhRepository = anhRepository;
    }

    @Transactional(readOnly = true)
    public SearchResult search(SearchRequest request, int limit) {
        int safeLimit = normalizeLimit(limit);
        List<ProductAggregate> catalog = loadCatalog();
        ResolvedRequest resolved = resolveCatalogFilters(request, catalog);
        List<ChatProductDTO> exact = filterAndSort(catalog, resolved, safeLimit);

        if (!exact.isEmpty()) {
            return new SearchResult(exact, true);
        }
        if (!resolved.hasRelaxableFilters()) {
            return new SearchResult(List.of(), true);
        }

        List<ChatProductDTO> suggestions = filterAndSort(
                catalog,
                resolved.relaxed(),
                safeLimit
        );
        return new SearchResult(suggestions, false);
    }

    @Transactional(readOnly = true)
    public List<ChatProductDTO> compareProducts(String message, int limit) {
        String normalizedMessage = normalizeText(message);
        List<MentionedProduct> mentioned = loadCatalog().stream()
                .map(product -> new MentionedProduct(
                        product,
                        mentionScore(normalizedMessage, product.product.getTen())
                ))
                .filter(item -> item.score() > 0)
                .sorted(Comparator
                        .comparingInt(MentionedProduct::score)
                        .reversed()
                        .thenComparing((MentionedProduct item) ->
                                item.product().product.getId()))
                .limit(Math.max(2, Math.min(limit, 3)))
                .toList();

        List<ChatProductDTO> result = new ArrayList<>();
        for (MentionedProduct item : mentioned) {
            result.add(toChatProduct(item.product(), item.product().variants));
        }
        return List.copyOf(result);
    }

    @Transactional(readOnly = true)
    public List<ChatProductDTO> findBestSelling(int limit) {
        return search(SearchRequest.empty(SortMode.BEST_SELLING), limit).products();
    }

    @Transactional(readOnly = true)
    public List<ChatProductDTO> findProductsUnderPrice(BigDecimal maxPrice, int limit) {
        return findProductsByPriceRange(null, maxPrice, limit);
    }

    @Transactional(readOnly = true)
    public List<ChatProductDTO> findProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int limit
    ) {
        return search(new SearchRequest(
                "", "", "", "", "", "", "", "",
                minPrice, maxPrice, false, SortMode.DEFAULT
        ), limit).products();
    }

    @Transactional(readOnly = true)
    public List<ChatProductDTO> searchProducts(
            String keyword,
            String gender,
            String size,
            String color,
            int limit
    ) {
        return search(new SearchRequest(
                "", keyword, "", "", "", gender, size, color,
                null, null, false, SortMode.DEFAULT
        ), limit).products();
    }

    private List<ChatProductDTO> filterAndSort(
            List<ProductAggregate> catalog,
            ResolvedRequest request,
            int limit
    ) {
        return catalog.stream()
                .map(product -> matchProduct(product, request))
                .filter(candidate -> candidate != null)
                .sorted(comparatorFor(request.sortMode))
                .limit(limit)
                .map(ProductCandidate::dto)
                .toList();
    }

    private ProductCandidate matchProduct(
            ProductAggregate aggregate,
            ResolvedRequest request
    ) {
        SanPham product = aggregate.product;

        if (!matchesProductText(product, request.keyword)
                || !matchesValue(product.getGioiTinh(), request.gender)
                || !matchesValue(brandOf(product), request.brand)
                || !matchesValue(categoryOf(product), request.category)
                || !matchesValue(product.getChatLieu(), request.material)) {
            return null;
        }

        List<BienTheSanPham> matchingVariants = aggregate.variants.stream()
                .filter(variant -> matchesVariant(variant, request))
                .toList();

        if (matchingVariants.isEmpty()) {
            return null;
        }

        ChatProductDTO dto = toChatProduct(aggregate, matchingVariants);
        int views = product.getLuotXem() == null ? 0 : product.getLuotXem();
        return new ProductCandidate(dto, relevanceScore(product, request), views);
    }

    private boolean matchesVariant(BienTheSanPham variant, ResolvedRequest request) {
        String size = variant.getKichCo() == null
                ? "" : variant.getKichCo().getTenKichCo();
        String color = variant.getMauSac() == null
                ? "" : variant.getMauSac().getTenMau();

        if (!matchesValue(size, request.size) || !matchesValue(color, request.color)) {
            return false;
        }

        BigDecimal price = effectivePrice(variant);
        if (price == null) {
            return false;
        }
        if (request.minPrice != null && price.compareTo(request.minPrice) < 0) {
            return false;
        }
        if (request.maxPrice != null && price.compareTo(request.maxPrice) > 0) {
            return false;
        }
        return !request.saleOnly || isOnSale(variant);
    }

    private boolean matchesProductText(SanPham product, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        String searchable = String.join(" ",
                safe(product.getTen()),
                safe(product.getMoTa()),
                safe(product.getChatLieu()),
                safe(categoryOf(product))
        );
        String normalizedSearchable = normalizeText(searchable);
        String normalizedKeyword = normalizeText(keyword);

        return normalizedSearchable.contains(normalizedKeyword)
                || significantTokens(normalizedKeyword).stream()
                .allMatch(normalizedSearchable::contains);
    }

    private boolean matchesValue(String actual, String expected) {
        return expected.isBlank()
                || normalizeText(safe(actual)).contains(normalizeText(expected));
    }

    private ResolvedRequest resolveCatalogFilters(
            SearchRequest request,
            List<ProductAggregate> catalog
    ) {
        String brand = safe(request.brand());
        String category = safe(request.category());
        String material = safe(request.material());
        String keyword = safe(request.keyword());

        String mentionedProduct = detectMentionedProduct(request.text(), catalog);
        if (!mentionedProduct.isBlank()) {
            keyword = mentionedProduct;
        }

        if (brand.isBlank()) {
            brand = detectCatalogValue(request.text(), catalog, item -> brandOf(item.product));
        }
        if (category.isBlank()) {
            category = detectCatalogValue(request.text(), catalog, item -> categoryOf(item.product));
        }
        if (material.isBlank()) {
            material = detectMaterial(request.text());
        }

        BigDecimal minPrice = positiveOrNull(request.minPrice());
        BigDecimal maxPrice = positiveOrNull(request.maxPrice());
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal temporary = minPrice;
            minPrice = maxPrice;
            maxPrice = temporary;
        }

        return new ResolvedRequest(
                safe(request.text()), keyword, brand, category, material,
                safe(request.gender()), safe(request.size()), safe(request.color()),
                minPrice, maxPrice, request.saleOnly(),
                request.sortMode() == null ? SortMode.DEFAULT : request.sortMode()
        );
    }

    private String detectMentionedProduct(
            String message,
            List<ProductAggregate> catalog
    ) {
        String normalizedMessage = normalizeText(message);
        return catalog.stream()
                .map(item -> new MentionedProduct(
                        item,
                        mentionScore(normalizedMessage, item.product.getTen())
                ))
                .filter(item -> item.score() > 0)
                .max(Comparator.comparingInt(MentionedProduct::score))
                .<String>map(item -> safe(item.product().product.getTen()))
                .orElse("");
    }

    private String detectCatalogValue(
            String message,
            List<ProductAggregate> catalog,
            Function<ProductAggregate, String> extractor
    ) {
        String normalizedMessage = normalizeText(message);
        return catalog.stream()
                .map(extractor)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(value -> normalizedMessage.contains(normalizeText(value)))
                .findFirst()
                .orElse("");
    }

    private String detectMaterial(String message) {
        String normalized = normalizeText(message);
        for (String material : List.of(
                "cotton", "linen", "denim", "kaki", "polyester",
                "ni", "len", "lua", "modal", "viscose"
        )) {
            if (containsWord(normalized, material)) {
                return material;
            }
        }
        return "";
    }

    private Comparator<ProductCandidate> comparatorFor(SortMode sortMode) {
        Comparator<ProductCandidate> byPrice = Comparator.comparing(
                item -> item.dto.price(),
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        Comparator<ProductCandidate> byPopularity = Comparator
                .comparingLong((ProductCandidate item) -> safeLong(item.dto.soldQuantity()))
                .reversed()
                .thenComparing(Comparator.comparingInt(ProductCandidate::views).reversed());

        return switch (sortMode) {
            case CHEAPEST -> byPrice.thenComparing(byPopularity);
            case MOST_EXPENSIVE -> byPrice.reversed().thenComparing(byPopularity);
            case BEST_SELLING -> byPopularity.thenComparing(byPrice);
            case FEATURED -> Comparator.comparingInt(ProductCandidate::views)
                    .reversed().thenComparing(byPopularity).thenComparing(byPrice);
            case DISCOUNT -> Comparator
                    .comparingInt((ProductCandidate item) -> safeInt(item.dto.discountPercent()))
                    .reversed().thenComparing(byPrice);
            case DEFAULT -> Comparator.comparingInt(ProductCandidate::relevance)
                    .reversed().thenComparing(byPopularity).thenComparing(byPrice);
        };
    }

    private int relevanceScore(SanPham product, ResolvedRequest request) {
        String normalizedName = normalizeText(product.getTen());
        int score = 0;
        for (String token : significantTokens(normalizeText(request.text))) {
            if (normalizedName.contains(token)) {
                score++;
            }
        }
        if (!request.keyword.isBlank()
                && normalizedName.contains(normalizeText(request.keyword))) {
            score += 5;
        }
        return score;
    }

    private int mentionScore(String message, String productName) {
        String normalizedName = normalizeText(productName);
        if (message.contains(normalizedName)) {
            return 20;
        }

        int score = 0;
        for (String token : significantTokens(normalizedName)) {
            if (!COMPARISON_STOP_WORDS.contains(token) && message.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private List<String> significantTokens(String value) {
        return Pattern.compile("[^a-z0-9]+")
                .splitAsStream(value)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private List<ProductAggregate> loadCatalog() {
        Map<Integer, ProductAggregate> products = new LinkedHashMap<>();

        for (BienTheSanPham variant : bienTheRepository.findAvailableForChatbot()) {
            SanPham product = variant.getSanPham();
            if (product == null || product.getId() == null) {
                continue;
            }
            products.computeIfAbsent(
                    product.getId(),
                    ignored -> new ProductAggregate(product)
            ).variants.add(variant);
        }
        return new ArrayList<>(products.values());
    }

    private ChatProductDTO toChatProduct(
            ProductAggregate aggregate,
            List<BienTheSanPham> variants
    ) {
        BienTheSanPham cheapest = variants.stream()
                .filter(variant -> effectivePrice(variant) != null)
                .min(Comparator.comparing(this::effectivePrice))
                .orElse(variants.get(0));
        BigDecimal price = effectivePrice(cheapest);
        BigDecimal originalPrice = isOnSale(cheapest) ? cheapest.getGia() : price;
        int stock = variants.stream().map(BienTheSanPham::getSoLuongTon)
                .filter(value -> value != null).mapToInt(Integer::intValue).sum();
        long sold = variants.stream().map(BienTheSanPham::getSoLuongDaBan)
                .filter(value -> value != null).mapToLong(Integer::longValue).sum();
        SanPham product = aggregate.product;

        return new ChatProductDTO(
                product.getId(), product.getTen(), price, originalPrice,
                discountPercent(originalPrice, price), findMainImage(product.getId()),
                stock, sold, brandOf(product), categoryOf(product),
                product.getChatLieu(), product.getGioiTinh(),
                distinctValues(variants, variant -> variant.getKichCo() == null
                        ? null : variant.getKichCo().getTenKichCo()),
                distinctValues(variants, variant -> variant.getMauSac() == null
                        ? null : variant.getMauSac().getTenMau()),
                "/product/" + product.getId()
        );
    }

    private List<String> distinctValues(
            List<BienTheSanPham> variants,
            Function<BienTheSanPham, String> extractor
    ) {
        Set<String> values = new LinkedHashSet<>();
        for (BienTheSanPham variant : variants) {
            String value = extractor.apply(variant);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private BigDecimal effectivePrice(BienTheSanPham variant) {
        return isOnSale(variant) ? variant.getGiaSale() : variant.getGia();
    }

    private boolean isOnSale(BienTheSanPham variant) {
        return variant.getGiaSale() != null
                && variant.getGiaSale().signum() > 0
                && variant.getGia() != null
                && variant.getGiaSale().compareTo(variant.getGia()) < 0;
    }

    private int discountPercent(BigDecimal originalPrice, BigDecimal price) {
        if (originalPrice == null || price == null || originalPrice.signum() <= 0
                || price.compareTo(originalPrice) >= 0) {
            return 0;
        }
        return originalPrice.subtract(price).multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP).intValue();
    }

    private String findMainImage(Integer productId) {
        List<AnhSanPham> images = anhRepository.findBySanPham_Id(productId);
        String url = images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getLaAnhChinh()))
                .map(AnhSanPham::getUrlAnh)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseGet(() -> images.stream().map(AnhSanPham::getUrlAnh)
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst().orElse(null));
        return normalizeImagePath(url);
    }

    private String normalizeImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String normalized = imageUrl.trim().replace("\\", "/");
        if (normalized.startsWith("http://") || normalized.startsWith("https://")
                || normalized.startsWith("/images/")) {
            return normalized;
        }
        if (normalized.startsWith("images/")) {
            return "/" + normalized;
        }
        if (normalized.startsWith("/products/")) {
            return "/images" + normalized;
        }
        if (normalized.startsWith("products/")) {
            return "/images/" + normalized;
        }
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private String brandOf(SanPham product) {
        return product.getThuongHieu() == null ? "" : safe(product.getThuongHieu().getTen());
    }

    private String categoryOf(SanPham product) {
        return product.getDanhMuc() == null ? "" : safe(product.getDanhMuc().getTen());
    }

    private boolean containsWord(String text, String word) {
        return Pattern.compile(
                "(^|[^a-z0-9])" + Pattern.quote(word) + "([^a-z0-9]|$)"
        ).matcher(text).find();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
    }

    private int normalizeLimit(int limit) {
        return Math.max(1, Math.min(limit, 5));
    }

    private BigDecimal positiveOrNull(BigDecimal value) {
        return value != null && value.signum() > 0 ? value : null;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public enum SortMode {
        DEFAULT, CHEAPEST, MOST_EXPENSIVE, BEST_SELLING, FEATURED, DISCOUNT
    }

    public record SearchRequest(
            String text, String keyword, String brand, String category,
            String material, String gender, String size, String color,
            BigDecimal minPrice, BigDecimal maxPrice,
            boolean saleOnly, SortMode sortMode
    ) {
        public static SearchRequest empty(SortMode mode) {
            return new SearchRequest(
                    "", "", "", "", "", "", "", "",
                    null, null, false, mode
            );
        }
    }

    public record SearchResult(List<ChatProductDTO> products, boolean exactMatch) {
    }

    private record ResolvedRequest(
            String text, String keyword, String brand, String category,
            String material, String gender, String size, String color,
            BigDecimal minPrice, BigDecimal maxPrice,
            boolean saleOnly, SortMode sortMode
    ) {
        boolean hasRelaxableFilters() {
            return !brand.isBlank() || !category.isBlank() || !material.isBlank()
                    || !gender.isBlank() || !size.isBlank() || !color.isBlank();
        }

        ResolvedRequest relaxed() {
            return new ResolvedRequest(
                    text, keyword, "", "", "", "", "", "",
                    minPrice, maxPrice, saleOnly, sortMode
            );
        }
    }

    private static final class ProductAggregate {
        private final SanPham product;
        private final List<BienTheSanPham> variants = new ArrayList<>();

        private ProductAggregate(SanPham product) {
            this.product = product;
        }
    }

    private record ProductCandidate(ChatProductDTO dto, int relevance, int views) {
    }

    private record MentionedProduct(ProductAggregate product, int score) {
    }
}
