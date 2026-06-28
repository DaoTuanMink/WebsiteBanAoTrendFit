package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Integer> {
    // Hàm này rất quan trọng để tìm các sản phẩm nằm trong 1 đơn hàng cụ thể
    List<ChiTietDonHang> findByDonHang_Id(Integer donHangId);

    boolean existsByBienTheSanPham_Id(Integer bienTheId);

      @Query("""
        select ct
        from ChiTietDonHang ct
        where ct.donHang.nguoiDung.id = :nguoiDungId
          and ct.bienTheSanPham.sanPham.id = :sanPhamId
          and ct.donHang.trangThai = 'DA_THANH_CONG'
        order by ct.donHang.ngayDat desc
    """)
    List<ChiTietDonHang> findChiTietDaMuaSanPham(
            @Param("nguoiDungId") Integer nguoiDungId,
            @Param("sanPhamId") Integer sanPhamId
    );
}