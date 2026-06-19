package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    // Tìm kiếm phân hệ Admin nâng cao
    List<SanPham> findByTenContainingIgnoreCaseAndDanhMucIdAndThuongHieuId(String ten, Integer danhMucId, Integer thuongHieuId);
    List<SanPham> findByTenContainingIgnoreCaseAndDanhMucId(String ten, Integer danhMucId);
    List<SanPham> findByTenContainingIgnoreCaseAndThuongHieuId(String ten, Integer thuongHieuId);
    List<SanPham> findByTenContainingIgnoreCase(String ten);
}