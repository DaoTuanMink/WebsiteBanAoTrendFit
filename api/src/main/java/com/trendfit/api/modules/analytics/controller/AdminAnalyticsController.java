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

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminAnalyticsController {

    private final JdbcTemplate jdbcTemplate;

    public AdminAnalyticsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*hihihi
     * API kiểm tra xem controller đã hoạt động chưa.
     * Mở trình duyệt:
     * http://localhost:8080/api/admin/analytics/test
     */
    @GetMapping("/test")
    public String test() {
        return "API thống kê doanh thu đang hoạt động";
    }

    /*
     * API chính dùng cho trang Dashboard Admin.
     * Ví dụ:
     * http://localhost:8080/api/admin/analytics/dashboard?type=month&from=2026-01-01&to=2026-12-31
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(
            @RequestParam(defaultValue = "month") String type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        LocalDate today = LocalDate.now();

        if (from == null) {
            from = LocalDate.of(today.getYear(), 1, 1);
        }

        if (to == null) {
            to = LocalDate.of(today.getYear(), 12, 31);
        }

        LocalDateTime fromDate = from.atStartOfDay();

        // Cộng thêm 1 ngày để lấy hết dữ liệu của ngày kết thúc
        LocalDateTime toDate = to.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = getTotalRevenue(fromDate, toDate);
        Long totalSuccessOrders = countSuccessOrders(fromDate, toDate);
        Long totalFailedOrders = countFailedOrders(fromDate, toDate);

        List<Map<String, Object>> revenueChart = getRevenueChart(type, fromDate, toDate);
        List<Map<String, Object>> orderStatusChart = getOrderStatusChart(fromDate, toDate);
        List<Map<String, Object>> topProducts = getTopProducts(fromDate, toDate);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalSuccessOrders", totalSuccessOrders);
        result.put("totalFailedOrders", totalFailedOrders);
        result.put("revenueChart", revenueChart);
        result.put("orderStatusChart", orderStatusChart);
        result.put("topProducts", topProducts);

        return result;
    }

    /*
     * Tính tổng doanh thu.
     * Chỉ tính các đơn đã giao / hoàn thành / thành công.
     */
    private BigDecimal getTotalRevenue(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
                SELECT COALESCE(SUM(dh.tong_thanh_toan), 0)
                FROM don_hang dh
                WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                  AND dh.ngay_dat >= ?
                  AND dh.ngay_dat < ?
                """;

        return jdbcTemplate.queryForObject(sql, BigDecimal.class, fromDate, toDate);
    }

    /*
     * Đếm số đơn hàng thành công.
     */
    private Long countSuccessOrders(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
                SELECT COUNT(*)
                FROM don_hang dh
                WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                  AND dh.ngay_dat >= ?
                  AND dh.ngay_dat < ?
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, fromDate, toDate);
    }

    /*
     * Đếm số đơn hàng thất bại hoặc bị hủy.
     */
    private Long countFailedOrders(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
                SELECT COUNT(*)
                FROM don_hang dh
                WHERE UPPER(dh.trang_thai) IN ('DA_HUY', 'HUY', 'THAT_BAI', 'FAILED', 'CANCELLED', 'CANCELED')
                  AND dh.ngay_dat >= ?
                  AND dh.ngay_dat < ?
                """;

        return jdbcTemplate.queryForObject(sql, Long.class, fromDate, toDate);
    }

    /*
     * Lấy dữ liệu doanh thu để vẽ biểu đồ.
     * type = day   : thống kê theo ngày
     * type = month : thống kê theo tháng
     * type = year  : thống kê theo năm
     */
    private List<Map<String, Object>> getRevenueChart(String type, LocalDateTime fromDate, LocalDateTime toDate) {
        String sql;

        if ("day".equalsIgnoreCase(type)) {
            sql = """
                    SELECT 
                        DATE_FORMAT(dh.ngay_dat, '%d/%m/%Y') AS label,
                        COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                      AND dh.ngay_dat >= ?
                      AND dh.ngay_dat < ?
                    GROUP BY DATE(dh.ngay_dat), DATE_FORMAT(dh.ngay_dat, '%d/%m/%Y')
                    ORDER BY DATE(dh.ngay_dat)
                    """;
        } else if ("year".equalsIgnoreCase(type)) {
            sql = """
                    SELECT 
                        CAST(YEAR(dh.ngay_dat) AS CHAR) AS label,
                        COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                      AND dh.ngay_dat >= ?
                      AND dh.ngay_dat < ?
                    GROUP BY YEAR(dh.ngay_dat)
                    ORDER BY YEAR(dh.ngay_dat)
                    """;
        } else {
            sql = """
                    SELECT 
                        DATE_FORMAT(dh.ngay_dat, '%m/%Y') AS label,
                        COALESCE(SUM(dh.tong_thanh_toan), 0) AS revenue
                    FROM don_hang dh
                    WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                      AND dh.ngay_dat >= ?
                      AND dh.ngay_dat < ?
                    GROUP BY DATE_FORMAT(dh.ngay_dat, '%Y-%m'), DATE_FORMAT(dh.ngay_dat, '%m/%Y')
                    ORDER BY DATE_FORMAT(dh.ngay_dat, '%Y-%m')
                    """;
        }

        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    /*
     * Thống kê số lượng đơn hàng theo từng trạng thái.
     * Dữ liệu này dùng để vẽ biểu đồ trạng thái đơn hàng.
     */
    private List<Map<String, Object>> getOrderStatusChart(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
                SELECT 
                    COALESCE(dh.trang_thai, 'KHONG_XAC_DINH') AS status,
                    CASE UPPER(COALESCE(dh.trang_thai, ''))
                        WHEN 'DA_GIAO' THEN 'Đã giao'
                        WHEN 'HOAN_THANH' THEN 'Hoàn thành'
                        WHEN 'THANH_CONG' THEN 'Thành công'
                        WHEN 'DA_HUY' THEN 'Đã hủy'
                        WHEN 'HUY' THEN 'Hủy'
                        WHEN 'THAT_BAI' THEN 'Thất bại'
                        WHEN 'CHO_XAC_NHAN' THEN 'Chờ xác nhận'
                        WHEN 'DANG_GIAO' THEN 'Đang giao'
                        ELSE COALESCE(dh.trang_thai, 'Không xác định')
                    END AS statusLabel,
                    COUNT(*) AS count
                FROM don_hang dh
                WHERE dh.ngay_dat >= ?
                  AND dh.ngay_dat < ?
                GROUP BY dh.trang_thai
                ORDER BY count DESC
                """;

        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    /*
     * Lấy top 5 sản phẩm bán chạy nhất.
     * Chỉ lấy từ những đơn hàng đã thành công.
     */
    private List<Map<String, Object>> getTopProducts(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
                SELECT 
                    ctdh.ten_san_pham AS productName,
                    SUM(ctdh.so_luong) AS quantitySold,
                    COALESCE(SUM(ctdh.thanh_tien), 0) AS revenue
                FROM chi_tiet_don_hang ctdh
                JOIN don_hang dh ON dh.id = ctdh.don_hang_id
                WHERE UPPER(dh.trang_thai) IN ('DA_GIAO', 'HOAN_THANH', 'THANH_CONG', 'SUCCESS', 'COMPLETED')
                  AND dh.ngay_dat >= ?
                  AND dh.ngay_dat < ?
                GROUP BY ctdh.ten_san_pham
                ORDER BY quantitySold DESC
                LIMIT 5
                """;

        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }
}