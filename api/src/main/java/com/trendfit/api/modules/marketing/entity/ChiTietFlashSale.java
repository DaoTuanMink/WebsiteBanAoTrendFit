package com.trendfit.api.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import com.trendfit.api.modules.product.entity.BienTheSanPham;

@Entity
@Table(name = "chi_tiet_flash_sale")
@Data
public class ChiTietFlashSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flash_sale_id")
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bien_the_id")
    private BienTheSanPham bienTheSanPham;

    private BigDecimal giaFlashSale;
    private Integer soLuongFlashSale;
    private Integer soLuongDaBan = 0;
    private Integer phanTramGiam;
}