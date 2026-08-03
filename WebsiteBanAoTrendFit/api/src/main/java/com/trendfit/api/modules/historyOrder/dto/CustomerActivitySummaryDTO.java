package com.trendfit.api.modules.historyOrder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tổng hợp "lịch sử hoạt động bản thân" của khách hàng :
 * - Tổng số đơn hàng đã mua thành công
 * - Tổng số tiền đã bỏ ra
 * - Danh sách sản phẩm mua nhiều nhất (top N)
 */
@Data
public class CustomerActivitySummaryDTO {
    private Integer tongSoDonHang;
    private BigDecimal tongTienDaChi;
    private List<TopProductDTO> sanPhamMuaNhieuNhat;
}
