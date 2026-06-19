package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    List<ThuongHieu> findByDangHoatDongTrueOrderByTenAsc();
}
