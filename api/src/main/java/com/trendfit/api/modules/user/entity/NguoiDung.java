package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Data
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    private String soDienThoai;
    private String vaiTro;
    private String anhDaiDien;
    private Boolean dangHoatDong = true;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}