package com.trendfit.api.modules.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO hiển thị 1 dòng lịch sử duyệt/thay đổi trạng thái đơn hàng,
 * kèm tên người đã thực hiện (yêu cầu của Thành).
 */
@Data
public class LichSuDonHangDTO {
    private Integer id;
    private String trangThaiCu;
    private String trangThaiMoi;
    private String ghiChu;
    private LocalDateTime ngayThayDoi;
    private String tenNguoiThucHien; // "Hệ thống" nếu không xác định được người thực hiện
}
