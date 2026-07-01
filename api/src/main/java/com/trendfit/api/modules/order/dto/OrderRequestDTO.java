package com.trendfit.api.modules.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {

    private String hoTen;

    private String sdt;

    private String diaChi;

    private String phuongThucThanhToan;

    private BigDecimal tongTienHang;

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