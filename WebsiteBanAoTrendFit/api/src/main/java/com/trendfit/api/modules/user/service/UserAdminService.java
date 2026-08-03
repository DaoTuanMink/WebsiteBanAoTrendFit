package com.trendfit.api.modules.user.service;

import com.trendfit.api.modules.user.dto.NhanVienDTO;
import com.trendfit.api.modules.user.entity.*;
import com.trendfit.api.modules.user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * Service xử lý nghiệp vụ quản lý tài khoản NHÂN VIÊN (dành cho ADMIN).
 *
 * Mô hình dữ liệu: 1 nhân viên = 1 bản ghi NguoiDung (dùng chung với khách
 * hàng, để đăng nhập 1 API duy nhất - xem AuthenController) + 1 bản ghi
 * NhanVien (thông tin nghiệp vụ riêng: mã NV, chức vụ, ngày vào làm...),
 * liên kết 1-1 qua nguoi_dung_id.
 */
@Service
public class UserAdminService {
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private NhanVienRepository nhanVienRepository;

    /**
     * Tạo tài khoản nhân viên mới.
     * Trước đây hàm này KHÔNG kiểm tra gì cả, nên khi email trống hoặc bị
     * trùng, Spring sẽ ném lỗi ràng buộc UNIQUE của DB (DataIntegrityViolationException)
     * -> trả về HTTP 500 với thông báo kỹ thuật khó hiểu, khiến người dùng
     * tưởng "bấm nút không có phản ứng gì". Giờ kiểm tra trước và báo lỗi
     * bằng tiếng Việt dễ hiểu.
     */
    @Transactional
    public void taoTaiKhoanNhanVien(NhanVienDTO dto) {
        validateDto(dto, true);
        if (nguoiDungRepository.findByEmail(dto.getEmail().trim()) != null) {
            throw new IllegalArgumentException("Email \"" + dto.getEmail() + "\" đã được sử dụng bởi tài khoản khác!");
        }

        if (nhanVienRepository.findByMaNhanVien(dto.getMaNhanVien().trim()) != null) {
            throw new IllegalArgumentException("Mã nhân viên \"" + dto.getMaNhanVien() + "\" đã tồn tại!");
        }

        // 1. Tạo tài khoản đăng nhập dùng chung (bảng nguoi_dung)
        NguoiDung user = new NguoiDung();
        user.setEmail(dto.getEmail().trim());
        // TODO (bảo mật): hiện mật khẩu đang lưu dạng chữ thường (plain text)
        // giống các tài khoản khách hàng khác trong hệ thống. Khi nâng cấp,
        // nên băm bằng BCryptPasswordEncoder ở đây VÀ ở AuthenController.login().
        user.setMatKhau(dto.getMatKhau());
        user.setHoTen(dto.getHoTen().trim());
        user.setSoDienThoai(dto.getSoDienThoai());
        user.setVaiTro("EMPLOYEE"); // Phân biệt với "CUSTOMER" và "ADMIN"
        user.setDangHoatDong(true);
        nguoiDungRepository.save(user);

        // 2. Tạo thông tin nghiệp vụ nhân viên, liên kết tới tài khoản vừa tạo
        NhanVien nv = new NhanVien();
        nv.setNguoiDung(user);
        nv.setMaNhanVien(dto.getMaNhanVien().trim());
        nv.setChucVu(dto.getChucVu());
        nv.setPhongBan(dto.getPhongBan());
        nv.setDangLamViec(true);
        nv.setNgayVaoLam(dto.getNgayVaoLam() != null ? dto.getNgayVaoLam() : LocalDate.now());
        nhanVienRepository.save(nv);
    }

    @Transactional
    public void xoaNhanVien(Integer id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + id));
        // Xóa tài khoản đăng nhập trước (nếu có), sau đó xóa bản ghi nhân viên
        if (nv.getNguoiDung() != null) {
            nguoiDungRepository.delete(nv.getNguoiDung());
        }
        nhanVienRepository.delete(nv);
    }

    /**
     * Cập nhật thông tin nhân viên. Email KHÔNG được thay đổi ở đây (frontend
     * cũng đã khoá ô nhập khi đang sửa) để tránh xung đột với ràng buộc
     * UNIQUE nếu vô tình nhập trùng email của người khác.
     */
    @Transactional
    public void capNhatNhanVien(Integer id, NhanVienDTO dto) {
        validateDto(dto, false);

        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên id=" + id));
        NguoiDung user = nv.getNguoiDung();

        if (user != null) {
            user.setHoTen(dto.getHoTen().trim());
            user.setSoDienThoai(dto.getSoDienThoai());
            nguoiDungRepository.save(user);
        }

        nv.setChucVu(dto.getChucVu());
        nv.setPhongBan(dto.getPhongBan());
        if (dto.getNgayVaoLam() != null) {
            nv.setNgayVaoLam(dto.getNgayVaoLam());
        }
        nhanVienRepository.save(nv);
    }

    /**
     * Kiểm tra dữ liệu bắt buộc trước khi lưu.
     * @param laTaoMoi true khi TẠO MỚI nhân viên (bắt buộc có email + mật khẩu,
     *                 vì lúc này mới tạo tài khoản đăng nhập lần đầu);
     *                 false khi CẬP NHẬT (màn hình sửa KHÔNG đổi email/mật khẩu,
     *                 ô email còn bị khóa ở phía FE, nên không được bắt buộc
     *                 nhập lại - nếu không nhân viên có email trống từ dữ liệu
     *                 cũ sẽ không bao giờ cập nhật được nữa).
     */
    private void validateDto(NhanVienDTO dto, boolean laTaoMoi) {
        if (!StringUtils.hasText(dto.getHoTen())) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (laTaoMoi && !StringUtils.hasText(dto.getEmail())) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (!StringUtils.hasText(dto.getMaNhanVien())) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống");
        }
        if (laTaoMoi && !StringUtils.hasText(dto.getMatKhau())) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
    }
}
