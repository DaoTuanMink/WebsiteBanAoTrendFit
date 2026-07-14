package com.trendfit.api.modules.product.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "kich_co")
@Data
public class KichCo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String tenKichCo; // Ví dụ: S, M, L, XL
}