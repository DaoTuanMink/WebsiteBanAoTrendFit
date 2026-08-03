package com.trendfit.api.modules.order.entity;

import com.trendfit.api.modules.user.entity.NguoiDung;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_don_hang")
@Data
public class LichSuDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id")
    private DonHang donHang;

    private String trangThaiCu;
    private String trangThaiMoi;
    private String ghiChu;
    private LocalDateTime ngayThayDoi;

    /**
     * Người đã thực hiện việc duyệt/thay đổi trạng thái đơn hàng
     * (yêu cầu của Thành: "Ghi lại lịch sử ai đã duyệt đơn hàng,
     * thay đổi trạng thái đơn hàng"). Có thể null nếu hệ thống tự
     * động cập nhật (ví dụ tác vụ nền) mà không qua nhân viên nào.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private NguoiDung nguoiThucHien;

    @PrePersist
    protected void onCreate() {
        ngayThayDoi = LocalDateTime.now();
    }
}