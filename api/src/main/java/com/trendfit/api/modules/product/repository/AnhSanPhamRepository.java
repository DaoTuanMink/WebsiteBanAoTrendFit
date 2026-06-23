package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.AnhSanPham;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnhSanPhamRepository extends JpaRepository<AnhSanPham, Integer> {
    // Lấy danh sách ảnh của sản phẩm
    List<AnhSanPham> findBySanPham_Id(Integer sanPhamId);
    
    // Xóa ảnh của sản phẩm
    @Modifying // <--- Rất quan trọng khi dùng lệnh delete/update custom
@Transactional
    void deleteBySanPham_Id(Integer sanPhamId);
}