package com.trendfit.api.modules.order.dto;

import lombok.Data;

import java.util.List;

/**
 * Dữ liệu ĐẦY ĐỦ trả về khi thu ngân bấm "Gọi lại" 1 hóa đơn chờ - FE dùng
 * để khôi phục lại đúng trạng thái giỏ hàng, khách hàng, voucher như lúc
 * tạm lưu, để tiếp tục thanh toán mà không cần chọn lại từ đầu.
 */
@Data
public class HoaDonChoDetailDTO {
    private Integer id;
    private String tenKhachHang;
    private String soDienThoai;
    private String phuongThucThanhToan;
    private String maVoucher;
    private Integer voucherId;
    private String ghiChu;
    private List<HoaDonChoItemDTO> items;
}
