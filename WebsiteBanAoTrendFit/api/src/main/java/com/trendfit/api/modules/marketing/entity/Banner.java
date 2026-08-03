package com.trendfit.api.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NhanVien;

@Entity
@Table(name = "banner")
@Data
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_tao_id")
    private NhanVien nguoiTao;

    private String tieuDe;
    private String urlAnh;
    private String urlLienKet;
    private Integer thuTu;
    private Boolean dangHoatDong = true;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}