package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.ChiTietGioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Integer> {
    List<ChiTietGioHang> findByGioHang_Id(Integer gioHangId);
    void deleteByGioHang_Id(Integer gioHangId);
}