package com.trendfit.api.modules.user.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileDTO {
    private Integer id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String anhDaiDien;
    
    // Danh sách địa chỉ nhận hàng của người dùng
    private List<DiaChiDTO> danhSachDiaChi;

    @Data
    public static class DiaChiDTO {
        private Integer id;
        private String tenNguoiNhan;
        private String soDienThoai;
        private String tinhThanh;
        private String xaPhuong;
        private String duong;
        private Boolean laMacDinh;
    }
}