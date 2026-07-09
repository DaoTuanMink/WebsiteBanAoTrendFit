package com.trendfit.api.modules.user.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class NhanVienDTO {
    private String hoTen;
    private String email;
    private String matKhau;
    private String maNhanVien;
    private String chucVu;
    private String soDienThoai; // Thêm
    private String phongBan;    // Thêm
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate ngayVaoLam;
}