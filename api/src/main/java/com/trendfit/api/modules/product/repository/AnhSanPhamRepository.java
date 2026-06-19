package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.AnhSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnhSanPhamRepository extends JpaRepository<AnhSanPham, Integer> {
    List<AnhSanPham> findBySanPham_IdOrderByThuTuAsc(Integer sanPhamId);

    void deleteBySanPham_Id(Integer sanPhamId);
}