package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dia_chi")
@Data
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String phuongXa;
    private String duong;
    private Boolean laMacDinh = false;
}