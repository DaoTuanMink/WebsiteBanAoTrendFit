package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "phan_quyen")
@Data
public class PhanQuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    private String tenQuyen;
    private String module;
    private Boolean docDuoc = false;
    private Boolean themDuoc = false;
    private Boolean suaDuoc = false;
    private Boolean xoaDuoc = false;
}