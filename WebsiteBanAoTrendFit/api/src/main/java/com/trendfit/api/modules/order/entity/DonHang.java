package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.user.entity.DiaChi;
import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "don_hang")
@Data
public class DonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dia_chi_id")
    private DiaChi diaChi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_giam_gia_id")
    private MaGiamGia maGiamGia;

    private String maDonHang;
    private BigDecimal tongTienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tienGiam;
    private Integer diemSuDung;
    private BigDecimal tongThanhToan;
    /*
 * NOTE DATN:
 * Hai trường này dùng cho nghiệp vụ bán hàng tại quầy.
 * tienKhachDua: số tiền khách đưa cho nhân viên.
 * tienThua: số tiền nhân viên cần trả lại khách.
 */
private BigDecimal tienKhachDua = BigDecimal.ZERO;

private BigDecimal tienThua = BigDecimal.ZERO;
    private String trangThai;
    private String phuongThucThanhToan;
    private String trangThaiHoanTra;
    private String tenNguoiNhan;
    private String diaChiGiao;
    private String soDienThoaiGiao;

    @Column(columnDefinition = "TEXT")
    private String ghiChu;

    private LocalDateTime ngayDat;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayDat = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}