package com.trendfit.api.modules.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 1 dòng sản phẩm trong giỏ hàng của "Hóa đơn chờ".
 * Được dùng cả khi LƯU (FE gửi lên) và khi TRẢ VỀ (FE nhận lại để khôi phục
 * đúng giỏ hàng cũ) - vì vậy cần đủ thông tin để FE render lại y hệt lúc lưu,
 * không cần gọi thêm API nào khác.
 */
@Data
public class HoaDonChoItemDTO {
    private Integer bienTheId;
    private String ten;
    private String maSku;
    private String tenKichCo;
    private String tenMau;
    private Integer quantity;
    private BigDecimal gia;
    private Integer soLuongTon; // để FE vẫn kiểm tra được giới hạn số lượng khi gọi lại
}
