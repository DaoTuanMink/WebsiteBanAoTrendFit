package com.trendfit.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "gio_hang")
@Data
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

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