package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByNguoiDungId(Integer nguoiDungId);
}