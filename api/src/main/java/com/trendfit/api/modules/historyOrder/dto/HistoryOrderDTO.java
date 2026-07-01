package com.trendfit.api.modules.historyOrder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HistoryOrderDTO {

    private Integer id;

    private String maDonHang;

    private LocalDateTime ngayDat;

    private BigDecimal tongThanhToan;

    private String trangThai;

    private String phuongThucThanhToan;

    private String tenNguoiNhan;

}
