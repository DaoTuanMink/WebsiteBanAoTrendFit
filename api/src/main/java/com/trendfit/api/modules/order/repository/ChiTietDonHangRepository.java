package com.trendfit.api.modules.order.repository;

import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import org.springframework.data.domain.Pageable;
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

    /**
     * Chức năng "Lịch sử hoạt động bản thân" (Việt):
     * Thống kê các sản phẩm khách hàng đã mua nhiều nhất, chỉ tính trên các
     * đơn hàng đã hoàn tất (DA_THANH_CONG). Gom nhóm theo sản phẩm gốc
     * (bienTheSanPham.sanPham) để cộng dồn số lượng dù khách mua nhiều
     * size/màu khác nhau của cùng 1 sản phẩm.
     * Kết quả được sắp xếp giảm dần theo số lượng đã mua.
     */
    @Query("""
        select
            ct.bienTheSanPham.sanPham.id as sanPhamId,
            ct.tenSanPham as tenSanPham,
            sum(ct.soLuong) as soLuongDaMua,
            sum(ct.thanhTien) as tongTienDaChi
        from ChiTietDonHang ct
        where ct.donHang.nguoiDung.id = :nguoiDungId
          and ct.donHang.trangThai = 'DA_THANH_CONG'
        group by ct.bienTheSanPham.sanPham.id, ct.tenSanPham
        order by sum(ct.soLuong) desc
    """)
    List<Object[]> thongKeSanPhamMuaNhieuNhat(
            @Param("nguoiDungId") Integer nguoiDungId,
            Pageable pageable
    );
}