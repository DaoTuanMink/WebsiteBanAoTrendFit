package com.trendfit.api.modules.historyOrder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Một dòng trong bảng "Sản phẩm mua nhiều nhất" của khách hàng.
 * sanPhamId có thể null nếu biến thể sản phẩm gốc đã bị xóa khỏi hệ thống,
 * khi đó chỉ còn giữ lại tên sản phẩm đã lưu snapshot trong ChiTietDonHang.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Integer sanPhamId;
    private String tenSanPham;
    private Long soLuongDaMua;
    private BigDecimal tongTienDaChi;
}
