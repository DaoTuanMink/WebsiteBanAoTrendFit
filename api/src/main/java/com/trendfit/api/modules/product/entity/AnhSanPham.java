package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "anh_san_pham")
@Data
public class AnhSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    private String urlAnh;
    private String altText;
    private Boolean laAnhChinh = false;
    private Integer thuTu = 0;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}