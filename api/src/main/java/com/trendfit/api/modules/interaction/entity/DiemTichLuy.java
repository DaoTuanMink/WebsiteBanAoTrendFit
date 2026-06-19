package com.trendfit.api.modules.interaction.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.trendfit.api.modules.user.entity.NguoiDung;

@Entity
@Table(name = "diem_tich_luy")
@Data
public class DiemTichLuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private Integer tongDiem = 0;
    private Integer diemCoTheDung = 0;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}