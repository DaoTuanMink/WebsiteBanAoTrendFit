package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_tai_khoan")
@Data
public class OauthTaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private String nhaProvide;
    private String provideId;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    private LocalDateTime ngayLienKet;

    @PrePersist
    protected void onCreate() { ngayLienKet = LocalDateTime.now(); }
}