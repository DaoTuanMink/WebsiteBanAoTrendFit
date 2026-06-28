package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.DonHang;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    // JpaRepository đã cung cấp sẵn các phương thức: 
    // save(), findById(), findAll(), deleteById(), ...
    List<DonHang> findByNguoiDung_IdOrderByNgayDatDesc(Integer nguoiDungId);
}