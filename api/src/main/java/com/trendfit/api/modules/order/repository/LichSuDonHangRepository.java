// File: api/src/main/java/com/trendfit/api/modules/order/repository/LichSuDonHangRepository.java
package com.trendfit.api.modules.order.repository;
import com.trendfit.api.modules.order.entity.LichSuDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuDonHangRepository extends JpaRepository<LichSuDonHang, Integer> {}