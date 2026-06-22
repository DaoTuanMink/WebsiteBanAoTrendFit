package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.BienTheSanPham;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    // Tìm theo sản phẩm cha
    List<BienTheSanPham> findBySanPham_Id(Integer sanPhamId);
    
    @Modifying
    @Transactional
    void deleteBySanPham_Id(Integer sanPhamId);

    
}