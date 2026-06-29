package com.trendfit.api.modules.user.service;

import com.trendfit.api.modules.user.dto.NhanVienDTO;
import com.trendfit.api.modules.user.entity.*;
import com.trendfit.api.modules.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserAdminService {
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private PhanQuyenRepository phanQuyenRepository; // Đã thêm

    @Transactional
    public void taoTaiKhoanNhanVien(NhanVienDTO dto) {
        // 1. Tạo tài khoản người dùng
        NguoiDung user = new NguoiDung();
        user.setEmail(dto.getEmail());
        user.setMatKhau(dto.getMatKhau());
        user.setHoTen(dto.getHoTen());
        user.setVaiTro("EMPLOYEE");
        nguoiDungRepository.save(user);

        // 2. Tạo thông tin nhân viên
        NhanVien nv = new NhanVien();
        nv.setNguoiDung(user);
        nv.setMaNhanVien(dto.getMaNhanVien());
        nv.setChucVu(dto.getChucVu());
        nhanVienRepository.save(nv);
    }

    @Transactional
    public void capNhatQuyen(Integer nhanVienId, List<PhanQuyen> danhSachQuyen) {
        // Xóa quyền cũ để gán quyền mới theo yêu cầu từ Admin
        phanQuyenRepository.deleteByNhanVienId(nhanVienId);
        
        // Gán nhân viên vào từng quyền và lưu
        NhanVien nv = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        
        for (PhanQuyen q : danhSachQuyen) {
            q.setNhanVien(nv);
        }
        phanQuyenRepository.saveAll(danhSachQuyen);
    }
}