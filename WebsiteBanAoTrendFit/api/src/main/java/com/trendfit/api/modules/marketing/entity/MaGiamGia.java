package com.trendfit.api.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ma_giam_gia")
@Data
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ma;
    private String ten;
    private String loai;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriToiDa;
    private BigDecimal donHangToiThieu;
    private Integer gioiHanSuDung;
    private Integer soLanDaDung = 0;
    private Boolean apDungChoTatCa = true;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Boolean dangHoatDong = true;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }
}