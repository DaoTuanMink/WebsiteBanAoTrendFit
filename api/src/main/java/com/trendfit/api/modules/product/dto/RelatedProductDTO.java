package com.trendfit.api.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO rút gọn dùng để hiển thị khối "Sản phẩm liên quan" ở trang chi tiết sản phẩm.
 * Chỉ chứa những trường cần thiết cho card hiển thị, tránh phải trả về
 * toàn bộ SanPham + BienTheSanPham + AnhSanPham như findByIdFull().
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedProductDTO {
    private Integer id;
    private String ten;
    private Float danhGiaTrungBinh;
    private Integer tongLuotDanhGia;
    private String anhChinh;
    private BigDecimal giaTu; // giá thấp nhất trong các biến thể, để hiển thị nếu cần
}
