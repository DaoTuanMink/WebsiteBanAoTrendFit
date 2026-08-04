package com.trendfit.api.modules.user.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Integer id;
    private String hoTen;
    private String email;
    private String matKhau;
    private String soDienThoai;
    private String anhDaiDien;
    private Boolean dangHoatDong;
}