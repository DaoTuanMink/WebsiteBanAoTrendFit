package com.trendfit.api.modules.analytics.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 *  API THỐNG KÊ DOANH SỐ — NOTE CHO NHÓM
 * ============================================================================
 * GET /api/admin/analytics/dashboard?type=month&from=2026-01-01&to=2026-12-31
 *
 * type  = day | month | year  → cách NHÓM cột trên biểu đồ doanh thu
 * from/to = khoảng thời gian lọc (không phụ thuộc type)
 *
 * Doanh thu / vốn / top SP chỉ tính đơn THÀNH CÔNG
 * (DA_THANH_CONG, DA_GIAO, HOAN_THANH, ...).
 * Biểu đồ trạng thái + totalOrders tính MỌI trạng thái trong khoảng.
 *
 * Role: ADMIN + EMPLOYEE (không nằm trong ADMIN_ONLY_PATHS).
 */
@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = {
        "http://localhost:5173", "http://localhost:5174",
        "http://localhost:5175", "http://localhost:3000"
}, allowCredentials = "true")
public class AdminAnalyticsController {

    private final JdbcTemplate jdbcTemplate;

    private static final String SUCCESS = """
            'DA_GIAO','HOAN_THANH','THANH_CONG','DA_THANH_CONG','SUCCESS','COMPLETED'
            """;
    private static final String FAILED = """
            'DA_HUY','HUY','THAT_BAI','FAILED','CANCELLED','CANCELED'
            """;

    public AdminAnalyticsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "API thống kê doanh thu đang hoạt động";
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        try {
            LocalDate today = LocalDate.now();
            if (from == null) from = LocalDate.of(today.getYear(), 1, 1);
            if (to == null) to = LocalDate.of(today.getYear(), 12, 31);
            if (from.isAfter(to)) {
                LocalDate tmp = from;
                from = to;
                to = tmp;
            }

            LocalDateTime fromDate = from.atStartOfDay();
            LocalDateTime toDate = to.plusDays(1).atStartOfDay();

            BigDecimal totalRevenue = sumRevenue(fromDate, toDate);
            BigDecimal totalImportCost = sumImportCost(fromDate, toDate);
            BigDecimal grossProfit = totalRevenue.subtract(totalImportCost);
            BigDecimal profitRate = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                profitRate = grossProfit.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 2, java.math.RoundingMode.HALF_UP);
            }

            Long totalSuccessOrders = countByStatus(fromDate, toDate, SUCCESS);
            Long totalFailedOrders = countByStatus(fromDate, toDate, FAILED);
            Long totalOrders = countAll(fromDate, toDate);
            Long totalPendingOrders = Math.max(0, totalOrders - totalSuccessOrders - totalFailedOrders);

            Long totalCustomers = countCustomers(fromDate, toDate);
            Long totalProductsSold = countProductsSold(fromDate, toDate);
            Long totalStock = countStock();
            BigDecimal avgOrderValue = BigDecimal.ZERO;
            if (totalSuccessOrders > 0) {
                avgOrderValue = totalRevenue.divide(
                        BigDecimal.valueOf(totalSuccessOrders), 0, java.math.RoundingMode.HALF_UP);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalRevenue", totalRevenue);
            result.put("totalImportCost", totalImportCost);
            result.put("grossProfit", grossProfit);
            result.put("profitRate", profitRate);
            result.put("totalSuccessOrders", totalSuccessOrders);
            result.put("totalFailedOrders", totalFailedOrders);
            result.put("totalOrders", totalOrders);
            result.put("totalPendingOrders", totalPendingOrders);
            result.put("totalCustomers", totalCustomers);
            result.put("totalProductsSold", totalProductsSold);
            result.put("totalStock", totalStock);
            result.put("avgOrderValue", avgOrderValue);
            result.put("revenueChart", getRevenueChart(type, fromDate, toDate));
            result.put("orderStatusChart", getOrderStatusChart(fromDate, toDate));
            result.put("topProducts", getTopProducts(fromDate, toDate));
            result.put("filterFrom", from.toString());
            result.put("filterTo", to.toString());
            result.put("groupBy", type);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            err.put("totalRevenue", BigDecimal.ZERO);
            err.put("totalImportCost", BigDecimal.ZERO);
            err.put("grossProfit", BigDecimal.ZERO);
            err.put("profitRate", BigDecimal.ZERO);
            err.put("totalSuccessOrders", 0L);
            err.put("totalFailedOrders", 0L);
            err.put("totalOrders", 0L);
            err.put("totalPendingOrders", 0L);
            err.put("totalCustomers", 0L);
            err.put("totalProductsSold", 0L);
            err.put("totalStock", 0L);
            err.put("avgOrderValue", BigDecimal.ZERO);
            err.put("revenueChart", List.of());
            err.put("orderStatusChart", List.of());
            err.put("topProducts", List.of());
            return err;
        }
    }

    private BigDecimal sumRevenue(LocalDateTime from, LocalDateTime to) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(dh.tong_thanh_toan), 0) FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    """.formatted(SUCCESS);
            BigDecimal r = jdbcTemplate.queryForObject(sql, BigDecimal.class, from, to);
            return r != null ? r : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Chỉ cộng vốn khi biến thể CÓ giá nhập (>0).
     * SP chưa nhập gia_nhap không bị coi là vốn = 0 (tránh lãi ảo 100%).
     */
    private BigDecimal sumImportCost(LocalDateTime from, LocalDateTime to) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(
                        CASE WHEN bt.gia_nhap IS NOT NULL AND bt.gia_nhap > 0
                             THEN ctdh.so_luong * bt.gia_nhap ELSE 0 END
                    ), 0)
                    FROM chi_tiet_don_hang ctdh
                    JOIN don_hang dh ON dh.id = ctdh.don_hang_id
                    LEFT JOIN bien_the_san_pham bt ON bt.id = ctdh.bien_the_id
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    """.formatted(SUCCESS);
            BigDecimal r = jdbcTemplate.queryForObject(sql, BigDecimal.class, from, to);
            return r != null ? r : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Long countByStatus(LocalDateTime from, LocalDateTime to, String statuses) {
        try {
            String sql = """
                    SELECT COUNT(*) FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    """.formatted(statuses);
            Long r = jdbcTemplate.queryForObject(sql, Long.class, from, to);
            return r != null ? r : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long countAll(LocalDateTime from, LocalDateTime to) {
        try {
            Long r = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM don_hang dh WHERE dh.ngay_dat >= ? AND dh.ngay_dat < ?",
                    Long.class, from, to);
            return r != null ? r : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long countCustomers(LocalDateTime from, LocalDateTime to) {
        try {
            Long r = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(DISTINCT dh.nguoi_dung_id) FROM don_hang dh
                    WHERE dh.nguoi_dung_id IS NOT NULL AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    """, Long.class, from, to);
            return r != null ? r : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long countProductsSold(LocalDateTime from, LocalDateTime to) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(ctdh.so_luong), 0)
                    FROM chi_tiet_don_hang ctdh
                    JOIN don_hang dh ON dh.id = ctdh.don_hang_id
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    """.formatted(SUCCESS);
            Long r = jdbcTemplate.queryForObject(sql, Long.class, from, to);
            return r != null ? r : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long countStock() {
        try {
            Long r = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(so_luong_ton), 0) FROM bien_the_san_pham", Long.class);
            return r != null ? r : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<Map<String, Object>> getRevenueChart(String type, LocalDateTime from, LocalDateTime to) {
        String sql;
        if ("day".equalsIgnoreCase(type)) {
            sql = """
                    SELECT DATE_FORMAT(dh.ngay_dat, '%%d/%%m/%%Y') AS label,
                           COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue,
                           COUNT(*) AS orderCount
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    GROUP BY DATE(dh.ngay_dat), DATE_FORMAT(dh.ngay_dat, '%%d/%%m/%%Y')
                    ORDER BY DATE(dh.ngay_dat)
                    """.formatted(SUCCESS);
        } else if ("year".equalsIgnoreCase(type)) {
            sql = """
                    SELECT CAST(YEAR(dh.ngay_dat) AS CHAR) AS label,
                           COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue,
                           COUNT(*) AS orderCount
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    GROUP BY YEAR(dh.ngay_dat) ORDER BY YEAR(dh.ngay_dat)
                    """.formatted(SUCCESS);
        } else {
            sql = """
                    SELECT DATE_FORMAT(dh.ngay_dat, '%%m/%%Y') AS label,
                           COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue,
                           COUNT(*) AS orderCount
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    GROUP BY DATE_FORMAT(dh.ngay_dat, '%%Y-%%m'), DATE_FORMAT(dh.ngay_dat, '%%m/%%Y')
                    ORDER BY DATE_FORMAT(dh.ngay_dat, '%%Y-%%m')
                    """.formatted(SUCCESS);
        }
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /**
     * NOTE NHÓM — Gộp trạng thái tương đương để legend dễ đọc, tránh
     * "Đã thành công / Đã giao / Hoàn thành" tách 3 màu xanh gây hiểu nhầm.
     * Nhóm:
     *   THANH_CONG  = DA_THANH_CONG, DA_GIAO, HOAN_THANH, THANH_CONG, SUCCESS, COMPLETED
     *   DANG_XU_LY  = CHO_XAC_NHAN, DA_XAC_NHAN, DANG_VAN_CHUYEN, DANG_GIAO
     *   HUY         = DA_HUY, HUY, THAT_BAI, FAILED, CANCELLED, CANCELED
     *   KHAC        = còn lại
     * Tổng count vẫn = totalOrders.
     */
    private List<Map<String, Object>> getOrderStatusChart(LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT
                    CASE
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('DA_THANH_CONG','DA_GIAO','HOAN_THANH','THANH_CONG','SUCCESS','COMPLETED')
                            THEN 'THANH_CONG'
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_VAN_CHUYEN','DANG_GIAO')
                            THEN 'DANG_XU_LY'
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('DA_HUY','HUY','THAT_BAI','FAILED','CANCELLED','CANCELED')
                            THEN 'HUY'
                        ELSE 'KHAC'
                    END AS status,
                    CASE
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('DA_THANH_CONG','DA_GIAO','HOAN_THANH','THANH_CONG','SUCCESS','COMPLETED')
                            THEN 'Thành công'
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_VAN_CHUYEN','DANG_GIAO')
                            THEN 'Đang xử lý'
                        WHEN UPPER(COALESCE(dh.trang_thai, '')) IN
                            ('DA_HUY','HUY','THAT_BAI','FAILED','CANCELLED','CANCELED')
                            THEN 'Đã hủy / thất bại'
                        ELSE 'Khác'
                    END AS statusLabel,
                    COUNT(*) AS count
                FROM don_hang dh
                WHERE dh.ngay_dat >= ? AND dh.ngay_dat < ?
                GROUP BY status, statusLabel
                ORDER BY count DESC
                """;
        return jdbcTemplate.queryForList(sql, from, to);
    }

    /**
     * NOTE NHÓM — Top SP:
     * - revenue: luôn có (don_gia trên chi tiết đơn)
     * - importCost: chỉ cộng dòng CÓ gia_nhap (không COALESCE 0)
     * - costKnown: true nếu >= 1 dòng có giá nhập
     * - profit / margin chỉ hợp lệ khi costKnown = true
     * Tránh hiển thị lãi 100% khi SP chưa nhập giá vốn.
     */
    private List<Map<String, Object>> getTopProducts(LocalDateTime from, LocalDateTime to) {
        try {
            String sql = """
                    SELECT ctdh.ten_san_pham AS productName,
                           SUM(ctdh.so_luong) AS quantitySold,
                           COALESCE(SUM(ctdh.so_luong * COALESCE(ctdh.don_gia, 0)), 0) AS revenue,
                           COALESCE(SUM(
                               CASE WHEN bt.gia_nhap IS NOT NULL AND bt.gia_nhap > 0
                                    THEN ctdh.so_luong * bt.gia_nhap ELSE 0 END
                           ), 0) AS importCost,
                           SUM(CASE WHEN bt.gia_nhap IS NOT NULL AND bt.gia_nhap > 0
                                    THEN ctdh.so_luong ELSE 0 END) AS qtyWithCost,
                           SUM(ctdh.so_luong) AS qtyTotal
                    FROM chi_tiet_don_hang ctdh
                    JOIN don_hang dh ON dh.id = ctdh.don_hang_id
                    LEFT JOIN bien_the_san_pham bt ON bt.id = ctdh.bien_the_id
                    WHERE UPPER(dh.trang_thai) IN (%s) AND dh.ngay_dat >= ? AND dh.ngay_dat < ?
                    GROUP BY ctdh.ten_san_pham
                    HAVING SUM(CASE WHEN bt.gia_nhap IS NOT NULL AND bt.gia_nhap > 0
                                    THEN ctdh.so_luong ELSE 0 END) > 0
                    ORDER BY quantitySold DESC
                    LIMIT 8
                    """.formatted(SUCCESS);
            // NOTE: chỉ lấy SP còn khớp biến thể có gia_nhap — ẩn dòng snapshot cũ / SP đã xóa (vốn null)
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, from, to);
            for (Map<String, Object> row : rows) {
                java.math.BigDecimal revenue = toBd(row.get("revenue"));
                java.math.BigDecimal importCost = toBd(row.get("importCost"));
                long qtyWith = toLong(row.get("qtyWithCost"));
                long qtyTotal = toLong(row.get("qtyTotal"));
                boolean costKnown = qtyWith > 0;
                row.put("costKnown", costKnown);
                row.put("costCoverage", qtyTotal > 0
                        ? java.math.BigDecimal.valueOf(qtyWith * 100.0 / qtyTotal)
                            .setScale(1, java.math.RoundingMode.HALF_UP)
                        : java.math.BigDecimal.ZERO);
                if (costKnown) {
                    java.math.BigDecimal profit = revenue.subtract(importCost);
                    row.put("profit", profit);
                    row.put("margin", revenue.compareTo(java.math.BigDecimal.ZERO) > 0
                            ? profit.multiply(java.math.BigDecimal.valueOf(100))
                                .divide(revenue, 2, java.math.RoundingMode.HALF_UP)
                            : java.math.BigDecimal.ZERO);
                } else {
                    // Không bịa lãi 100% khi chưa có giá nhập
                    row.put("profit", null);
                    row.put("margin", null);
                    row.put("importCost", null);
                }
            }
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private java.math.BigDecimal toBd(Object v) {
        if (v == null) return java.math.BigDecimal.ZERO;
        if (v instanceof java.math.BigDecimal) return (java.math.BigDecimal) v;
        try { return new java.math.BigDecimal(v.toString()); } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }
}

