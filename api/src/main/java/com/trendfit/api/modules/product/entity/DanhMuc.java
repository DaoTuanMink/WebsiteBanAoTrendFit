package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "danh_muc")
@Data
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "danh_muc_cha_id")
    private Integer danhMucChaId;

    private String ten;
    private String slug;
    private String urlAnh;
    private Integer thuTu;
    private Boolean dangHoatDong = true;
}