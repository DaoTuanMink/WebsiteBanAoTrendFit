package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
@Data
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thuong_hieu_id")
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "danh_muc_id")
    private DanhMuc danhMuc;

    @Column(nullable = false)
    private String ten;
    
    private String slug;
    private String gioiTinh;
    private String chatLieu;
    private String xuatXu;
    private Integer namRaMat;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    @Column(columnDefinition = "TEXT")
    private String thanhPhanChatLieu;

    private Float danhGiaTrungBinh = 0.0f;
    private Integer tongLuotDanhGia = 0;
    private Integer luotXem = 0;
    private Boolean dangBan = true;
    
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