package com.trendfit.api.modules.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 1 dòng trong danh sách "Hóa đơn chờ" (khi thu ngân bấm xem danh sách các
 * đơn đang tạm lưu). Chỉ chứa thông tin tóm tắt để hiển thị nhanh - KHÔNG
 * kèm chi tiết từng sản phẩm (xem HoaDonChoDetailDTO khi cần gọi lại đầy đủ).
 */
@Data
public class HoaDonChoDTO {
    private Integer id;
    private String tenKhachHang;
    private String soDienThoai;
    private Integer soLuongSanPham;
    private BigDecimal tongTien;
    private String ghiChu;
    private String tenNguoiTao;
    private LocalDateTime ngayTao;
}
