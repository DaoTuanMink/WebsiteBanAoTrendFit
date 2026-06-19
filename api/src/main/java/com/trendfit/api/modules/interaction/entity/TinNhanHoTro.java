package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "tin_nhan_ho_tro")
@Data
public class TinNhanHoTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ho_tro_id")
    private HoTroKhachHang hoTroKhachHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_gui_id")
    private NguoiDung nguoiGui;

    private String noiDung;
    private String vaiTroGui;
    private Boolean daDoc = false;
    private LocalDateTime ngayGui;

    @PrePersist
    protected void onCreate() { ngayGui = LocalDateTime.now(); }
}