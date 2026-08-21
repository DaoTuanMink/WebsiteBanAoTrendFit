package com.trendfit.api.modules.marketing.repository;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Integer> {
    Optional<MaGiamGia> findByMa(String ma);

    // THÊM METHOD NÀY ĐỂ ÉP DATABASE TĂNG SỐ LẦN DÙNG NGAY LẬP TỨC
    @Modifying
    @Transactional
    @Query("UPDATE MaGiamGia m SET m.soLanDaDung = COALESCE(m.soLanDaDung, 0) + 1 WHERE m.id = :id")
    int tangSoLanDaDung(@Param("id") Integer id);
}