package com.trendfit.api.modules.user.service;

import com.trendfit.api.modules.user.dto.CustomerDTO;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerAdminService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    // Lấy danh sách toàn bộ khách hàng (vaiTro = 'CUSTOMER')
    public List<NguoiDung> getAllCustomers() {
        return nguoiDungRepository.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getVaiTro()))
                .collect(Collectors.toList());
    }

    // Thêm mới khách hàng (Admin tạo hộ nếu cần)
    @Transactional
    public NguoiDung taoKhachHang(CustomerDTO dto) {
        if (!StringUtils.hasText(dto.getEmail()) || !StringUtils.hasText(dto.getMatKhau())) {
            throw new IllegalArgumentException("Email và mật khẩu không được để trống!");
        }
        if (nguoiDungRepository.findByEmail(dto.getEmail().trim()) != null) {
            throw new IllegalArgumentException("Email \"" + dto.getEmail() + "\" đã tồn tại!");
        }

        NguoiDung user = new NguoiDung();
        user.setHoTen(StringUtils.hasText(dto.getHoTen()) ? dto.getHoTen().trim() : "Khách hàng mới");
        user.setEmail(dto.getEmail().trim());
        user.setMatKhau(dto.getMatKhau()); // Lưu ý: có thể băm BCrypt nếu muốn
        user.setSoDienThoai(dto.getSoDienThoai());
        user.setVaiTro("CUSTOMER");
        user.setDangHoatDong(true);
        user.setAnhDaiDien(dto.getAnhDaiDien());

        return nguoiDungRepository.save(user);
    }

    // Cập nhật thông tin khách hàng
    @Transactional
    public NguoiDung capNhatKhachHang(Integer id, CustomerDTO dto) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng ID = " + id));

        if (StringUtils.hasText(dto.getHoTen())) {
            user.setHoTen(dto.getHoTen().trim());
        }
        if (dto.getSoDienThoai() != null) {
            user.setSoDienThoai(dto.getSoDienThoai());
        }
        if (dto.getDangHoatDong() != null) {
            user.setDangHoatDong(dto.getDangHoatDong());
        }
        if (StringUtils.hasText(dto.getAnhDaiDien())) {
            user.setAnhDaiDien(dto.getAnhDaiDien());
        }

        return nguoiDungRepository.save(user);
    }

    // Đổi trạng thái Khóa / Hoạt động
    @Transactional
    public void toggleTrangThai(Integer id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng ID = " + id));
        user.setDangHoatDong(user.getDangHoatDong() == null || !user.getDangHoatDong());
        nguoiDungRepository.save(user);
    }

    // Xóa khách hàng
    @Transactional
    public void xoaKhachHang(Integer id) {
        if (!nguoiDungRepository.existsById(id)) {
            throw new IllegalArgumentException("Khách hàng không tồn tại!");
        }
        nguoiDungRepository.deleteById(id);
    }
}