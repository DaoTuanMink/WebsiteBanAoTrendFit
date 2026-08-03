package com.trendfit.api.modules.historyOrder.repository;

import com.trendfit.api.modules.order.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryOrderRepository extends JpaRepository<DonHang, Integer> {

    List<DonHang> findByNguoiDung_IdAndTrangThaiOrderByNgayDatDesc(
            Integer nguoiDungId,
            String trangThai
    );

}
