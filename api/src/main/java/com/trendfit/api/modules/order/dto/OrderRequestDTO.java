package com.trendfit.api.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OrderRequestDTO {

    private String hoTen;

    private String sdt;

    private String diaChi;

    // Tỉnh/thành khách CHỌN (không gõ tay) ở combobox trên FE - dùng để
    // server tự tra ra vùng miền và tính phí ship, xem
    // OrderService.tinhPhiShipGoiY(). Chỉ áp dụng cho đơn ĐẶT ONLINE.
    private String tinhThanh;

    private String phuongThucThanhToan;

    private BigDecimal tongTienHang;

    // Phí vận chuyển - chỉ áp dụng cho đơn ĐẶT ONLINE (khách tự nhập địa chỉ
    // giao hàng). Đơn BÁN TẠI QUẦY luôn gửi lên 0 (xem AdminPosView.vue).
    private BigDecimal phiVanChuyen;

    private BigDecimal tienGiam;

    private BigDecimal tongThanhToan;

    private BigDecimal tienKhachDua;

    private BigDecimal tienThua;

    private Integer voucherId;

    private String maVoucher;

    @JsonProperty("userId")
    private Integer userId;

    private Integer creatorId;

    private List<OrderItemDTO> items;

    
}