package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.HoaDonCho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChoRepository extends JpaRepository<HoaDonCho, Integer> {
    // Danh sách hóa đơn chờ, mới tạm lưu gần nhất hiển thị lên đầu
    List<HoaDonCho> findAllByOrderByNgayTaoDesc();
}
