package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.trendfit.api.modules.product.entity.BienTheSanPham;

@Entity
@Table(name = "chi_tiet_don_hang")
@Data
public class ChiTietDonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "don_hang_id")
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bien_the_id")
    private BienTheSanPham bienTheSanPham;

    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String tenSanPham;
    private String maSku;
    private String kichCoSize;
    private String mauSac;
}