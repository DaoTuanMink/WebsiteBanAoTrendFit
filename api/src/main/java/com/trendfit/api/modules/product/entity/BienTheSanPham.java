package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bien_the_san_pham")
@Data
public class BienTheSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    private String kichCoSize;
    private String mauSac;
    private BigDecimal gia;
    private BigDecimal giaSale;
    private Integer soLuongTon = 0;
    private Integer soLuongDaBan = 0;
    private String maSku;
    private Boolean dangBan = true;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}