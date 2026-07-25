package com.trendfit.api.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Data
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    private String soDienThoai;
    private String vaiTro;
    private String anhDaiDien;
    private Boolean dangHoatDong = true;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    /**
     * Phục vụ chức năng "Quên mật khẩu": khi người dùng yêu cầu, hệ thống sinh
     * 1 mã gồm 6 chữ số, lưu tạm ở đây kèm thời điểm hết hạn (5 phút), gửi qua
     * email (xem EmailService.sendVerificationCode). Khi người dùng nhập đúng
     * mã còn hạn ở bước đặt lại mật khẩu, 2 field này được xóa về null.
     * Xem PasswordResetController để biết luồng đầy đủ.
     */
    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_het_han")
    private LocalDateTime resetCodeHetHan;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }
}