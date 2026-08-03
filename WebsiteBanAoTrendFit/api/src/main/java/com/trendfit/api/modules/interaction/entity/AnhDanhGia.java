package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "anh_danh_gia")
@Data
public class AnhDanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "danh_gia_id")
    private DanhGia danhGia;

    private String urlAnh;
    private Integer thuTu = 0;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}