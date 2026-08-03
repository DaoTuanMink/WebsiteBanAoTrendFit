package com.trendfit.api.modules.interaction.repository;

import com.trendfit.api.modules.interaction.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    @Query("""
        select dg
        from DanhGia dg
        where dg.sanPham.id = :sanPhamId
          and dg.daDuyet = true
        order by dg.ngayTao desc
    """)
    List<DanhGia> findBySanPham_IdAndDaDuyetTrueOrderByNgayTaoDesc(
            @Param("sanPhamId") Integer sanPhamId
    );

    @Query("""
        select dg
        from DanhGia dg
        where dg.sanPham.id = :sanPhamId
          and dg.daDuyet = true
        order by dg.ngayTao desc
    """)
    List<DanhGia> findDanhGiaHienThiTheoSanPham(
            @Param("sanPhamId") Integer sanPhamId
    );

    boolean existsByNguoiDung_IdAndSanPham_Id(Integer nguoiDungId, Integer sanPhamId);

    long countBySanPham_IdAndDaDuyetTrue(Integer sanPhamId);

    @Query("""
        select coalesce(avg(dg.saoXepHang), 0)
        from DanhGia dg
        where dg.sanPham.id = :sanPhamId
          and dg.daDuyet = true
    """)
    Double tinhDiemTrungBinh(@Param("sanPhamId") Integer sanPhamId);
}