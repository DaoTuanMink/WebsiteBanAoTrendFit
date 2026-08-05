package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.YeuCauHoanTra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YeuCauHoanTraRepository extends JpaRepository<YeuCauHoanTra, Integer> {
    Optional<YeuCauHoanTra> findByDonHang_Id(Integer donHangId);
}