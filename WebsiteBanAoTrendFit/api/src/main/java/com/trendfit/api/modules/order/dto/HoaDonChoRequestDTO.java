package com.trendfit.api.modules.order.dto;

import lombok.Data;

import java.util.List;

/**
 * Dữ liệu FE gửi lên khi thu ngân bấm nút "Lưu tạm / Hóa đơn chờ" tại màn
 * hình POS. Chứa toàn bộ trạng thái đang thao tác dở dang (khách hàng,
 * phương thức thanh toán, voucher đã áp, và giỏ hàng) để có thể khôi phục
 * lại y hệt khi gọi lại sau này.
 */
@Data
public class HoaDonChoRequestDTO {
    private String tenKhachHang;
    private String soDienThoai;
    private String phuongThucThanhToan;
    private String maVoucher;
    private Integer voucherId;
    private String ghiChu;
    private List<HoaDonChoItemDTO> items;
}
