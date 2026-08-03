package com.trendfit.api.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NhanVien;

@Entity
@Table(name = "flash_sale")
@Data
public class FlashSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_tao_id")
    private NhanVien nguoiTao;

    private String ten;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private Boolean dangHoatDong = true;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}