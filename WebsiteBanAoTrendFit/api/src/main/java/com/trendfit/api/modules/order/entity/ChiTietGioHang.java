package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.product.entity.BienTheSanPham;

@Entity
@Table(name = "chi_tiet_gio_hang")
@Data
public class ChiTietGioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gio_hang_id")
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bien_the_id")
    private BienTheSanPham bienTheSanPham;

    private Integer soLuong;
    private LocalDateTime ngayThem;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayThem = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}