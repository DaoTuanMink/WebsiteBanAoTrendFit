package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "thuong_hieu")
@Data
public class ThuongHieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ten;
    private String quocGia;
    private String urlLogo;

    @Column(columnDefinition = "TEXT")
    private String moTa;
    
    private Boolean dangHoatDong = true;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}