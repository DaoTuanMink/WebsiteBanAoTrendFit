package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "lich_su_xem_san_pham")
@Data
public class LichSuXemSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    private Integer soLanXem = 1;
    private LocalDateTime lanXemCuoi;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { lanXemCuoi = LocalDateTime.now(); }
}