package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
}