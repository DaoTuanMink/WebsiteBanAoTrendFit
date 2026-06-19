package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
@Data
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "don_hang_id")
    private DonHang donHang;

    private String phuongThuc;
    private BigDecimal soTien;
    private String trangThai;
    private String maGiaoDich;
    private String nhaProvide;

    @Column(columnDefinition = "TEXT")
    private String duLieuPhanHoi;

    private LocalDateTime thoiGianThanhToan;

    @PrePersist
    protected void onCreate() { thoiGianThanhToan = LocalDateTime.now(); }
}