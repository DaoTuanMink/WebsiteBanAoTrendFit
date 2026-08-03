package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.entity.NhanVien;

@Entity
@Table(name = "ho_tro_khach_hang")
@Data
public class HoTroKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nhan_vien_phu_trach_id")
    private NhanVien nhanVienPhuTrach;

    private String tieuDe;
    private String loai;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayDong;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}