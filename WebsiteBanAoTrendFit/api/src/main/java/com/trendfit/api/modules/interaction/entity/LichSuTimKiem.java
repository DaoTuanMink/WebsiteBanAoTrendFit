package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "lich_su_tim_kiem")
@Data
public class LichSuTimKiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private String tuKhoa;
    private Integer soLanTimKiem = 1;
    private LocalDateTime lanTimCuoi;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { lanTimCuoi = LocalDateTime.now(); }
}