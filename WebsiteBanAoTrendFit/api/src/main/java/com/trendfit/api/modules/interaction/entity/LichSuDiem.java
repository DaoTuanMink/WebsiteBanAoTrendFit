package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "lich_su_diem")
@Data
public class LichSuDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "don_hang_id")
    private DonHang donHang;

    private Integer soDiem;
    private String loaiGiaoDich;
    private String moTa;
    private LocalDateTime ngayGiaoDich;

    @PrePersist
    protected void onCreate() { ngayGiaoDich = LocalDateTime.now(); }
}