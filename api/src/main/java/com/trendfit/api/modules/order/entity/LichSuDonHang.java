package com.trendfit.api.modules.order.entity;

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

    @PrePersist
    protected void onCreate() {
        ngayThayDoi = LocalDateTime.now();
    }
}