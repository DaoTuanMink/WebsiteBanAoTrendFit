package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "danh_gia")
@Data
public class DanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chi_tiet_don_hang_id")
    private ChiTietDonHang chiTietDonHang;

    private Integer saoXepHang;
    private String tieuDe;

    @Column(columnDefinition = "TEXT")
    private String noiDung;

    private Boolean daDuyet = false;
    private Integer luotThich = 0;
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