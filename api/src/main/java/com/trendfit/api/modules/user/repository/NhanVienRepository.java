package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    // Có thể thêm các hàm tìm kiếm tùy chọn ở đây nếu cần
}