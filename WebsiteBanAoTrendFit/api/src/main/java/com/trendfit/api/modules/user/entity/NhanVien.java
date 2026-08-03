package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "nhan_vien")
@Data
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @Column(unique = true)
    private String maNhanVien;
    
    private String chucVu;
    private String phongBan;
    private Boolean dangLamViec = true;
    private LocalDate ngayVaoLam;
}