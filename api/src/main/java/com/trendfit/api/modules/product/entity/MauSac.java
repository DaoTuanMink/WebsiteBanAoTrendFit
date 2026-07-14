package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mau_sac")
@Data
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String tenMau; // Ví dụ: Đỏ, Xanh, Đen

    private String maMau; // Ví dụ: #FF0000 (để lưu mã màu CSS nếu cần)
}