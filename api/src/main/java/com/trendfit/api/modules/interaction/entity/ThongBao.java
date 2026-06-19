package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "thong_bao")
@Data
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private String loai;
    private String tieuDe;
    private String noiDung;
    private String urlLienKet;
    private Boolean daDoc = false;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}