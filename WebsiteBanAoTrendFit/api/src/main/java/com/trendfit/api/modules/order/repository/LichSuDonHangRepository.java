package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.LichSuDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuDonHangRepository extends JpaRepository<LichSuDonHang, Integer> {
    // Lấy toàn bộ lịch sử thay đổi trạng thái của 1 đơn hàng, mới nhất lên đầu
    List<LichSuDonHang> findByDonHang_IdOrderByNgayThayDoiDesc(Integer donHangId);
}
