package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "yeu_cau_hoan_tra")
@Data
public class YeuCauHoanTra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "don_hang_id")
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chi_tiet_don_hang_id")
    private ChiTietDonHang chiTietDonHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private Integer soLuongHoan;
    private String lyDo;

    @Column(columnDefinition = "TEXT")
    private String moTaChiTiet;

    private String trangThai;
    private BigDecimal soTienHoan;
    private String phuongThucHoan;
    private LocalDateTime ngayYeuCau;
    private LocalDateTime ngayXuLy;

    @PrePersist
    protected void onCreate() { ngayYeuCau = LocalDateTime.now(); }
}