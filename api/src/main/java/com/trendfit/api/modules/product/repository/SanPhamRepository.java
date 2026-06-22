package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    // Tìm kiếm cơ bản phục vụ cho trang Admin (lọc theo danh mục/thương hiệu)
    List<SanPham> findByDanhMuc_IdAndThuongHieu_Id(Integer danhMucId, Integer thuongHieuId);
    List<SanPham> findByTenContainingIgnoreCase(String ten);
}