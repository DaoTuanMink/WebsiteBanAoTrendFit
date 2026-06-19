package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    List<BienTheSanPham> findBySanPham_Id(Integer sanPhamId);

    void deleteBySanPham_Id(Integer sanPhamId);
}