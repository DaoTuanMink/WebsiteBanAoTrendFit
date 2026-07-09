package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.NhanVien;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    // Có thể thêm các hàm tìm kiếm tùy chọn ở đây nếu cần

    // Tìm kiếm nhân viên có mã chứa chuỗi truyền vào (không phân biệt hoa thường nếu cấu hình đúng)
    // Containing tương đương với toán tử LIKE '%...%' trong SQL
    List<NhanVien> findByMaNhanVienContaining(String maNhanVien);
}