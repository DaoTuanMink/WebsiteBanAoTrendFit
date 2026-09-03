package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.BienTheSanPham;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BienTheSanPhamRepository
        extends JpaRepository<BienTheSanPham, Integer> {

    // Tìm biến thể theo sản phẩm cha
    List<BienTheSanPham> findBySanPham_Id(Integer sanPhamId);

    @Modifying
    @Transactional
    void deleteBySanPham_Id(Integer sanPhamId);

    /**
     * Trừ tồn kho nguyên tử.
     *
     * @return 1 nếu trừ thành công, 0 nếu không đủ tồn kho.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        update BienTheSanPham b
           set b.soLuongTon = b.soLuongTon - :soLuong,
               b.soLuongDaBan = coalesce(b.soLuongDaBan, 0) + :soLuong
         where b.id = :id
           and b.soLuongTon >= :soLuong
    """)
    int truTonKhoNguyenTu(
            @Param("id") Integer id,
            @Param("soLuong") int soLuong
    );

    /**
     * Hoàn lại tồn kho khi đơn hàng bị hủy.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        update BienTheSanPham b
           set b.soLuongTon = b.soLuongTon + :soLuong,
               b.soLuongDaBan = case
                   when coalesce(b.soLuongDaBan, 0) - :soLuong < 0 then 0
                   else coalesce(b.soLuongDaBan, 0) - :soLuong
               end
         where b.id = :id
    """)
    int hoanTonKhoNguyenTu(
            @Param("id") Integer id,
            @Param("soLuong") int soLuong
    );

    /**
     * Lấy sản phẩm nổi bật cho chatbot.
     *
     * Ưu tiên số lượng đã bán, lượt xem và ngày tạo.
     */
    @Query("""
        select
            bt.sanPham.id,
            bt.sanPham.ten,
            min(coalesce(bt.giaSale, bt.gia)),
            sum(coalesce(bt.soLuongTon, 0)),
            sum(coalesce(bt.soLuongDaBan, 0))
        from BienTheSanPham bt
        where bt.sanPham.dangBan = true
          and bt.dangBan = true
          and bt.soLuongTon > 0
        group by bt.sanPham.id, bt.sanPham.ten
        order by
            sum(coalesce(bt.soLuongDaBan, 0)) desc,
            max(bt.sanPham.luotXem) desc,
            max(bt.sanPham.ngayTao) desc
    """)
    List<Object[]> findBestSellingForChatbot(Pageable pageable);

    /**
     * Tìm sản phẩm có giá không vượt quá mức khách hàng yêu cầu.
     *
     * Chỉ lấy sản phẩm đang bán, biến thể đang bán và còn hàng.
     */
    @Query("""
        select
            bt.sanPham.id,
            bt.sanPham.ten,
            min(coalesce(bt.giaSale, bt.gia)),
            sum(coalesce(bt.soLuongTon, 0)),
            sum(coalesce(bt.soLuongDaBan, 0))
        from BienTheSanPham bt
        where bt.sanPham.dangBan = true
          and bt.dangBan = true
          and bt.soLuongTon > 0
          and coalesce(bt.giaSale, bt.gia) <= :maxPrice
        group by bt.sanPham.id, bt.sanPham.ten
        order by
            sum(coalesce(bt.soLuongDaBan, 0)) desc,
            min(coalesce(bt.giaSale, bt.gia)) asc
    """)
    List<Object[]> findProductsUnderPriceForChatbot(
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /**
 * Tìm sản phẩm cho chatbot theo nhiều tiêu chí.
 *
 * Tham số chuỗi rỗng có nghĩa là khách không yêu cầu tiêu chí đó.
 */
@Query("""
    select
        bt.sanPham.id,
        bt.sanPham.ten,
        min(coalesce(bt.giaSale, bt.gia)),
        sum(coalesce(bt.soLuongTon, 0)),
        sum(coalesce(bt.soLuongDaBan, 0))
    from BienTheSanPham bt
    where bt.sanPham.dangBan = true
      and bt.dangBan = true
      and bt.soLuongTon > 0

      and (
          :keyword = ''
          or lower(bt.sanPham.ten)
             like concat('%', lower(:keyword), '%')
          or lower(bt.sanPham.moTa)
             like concat('%', lower(:keyword), '%')
      )

      and (
          :gender = ''
          or lower(bt.sanPham.gioiTinh)
             like concat('%', lower(:gender), '%')
      )

      and (
          :size = ''
          or lower(bt.kichCo.tenKichCo) = lower(:size)
      )

      and (
          :color = ''
          or lower(bt.mauSac.tenMau)
             like concat('%', lower(:color), '%')
      )

    group by bt.sanPham.id, bt.sanPham.ten
    order by
        sum(coalesce(bt.soLuongDaBan, 0)) desc,
        max(bt.sanPham.luotXem) desc,
        min(coalesce(bt.giaSale, bt.gia)) asc
""")
List<Object[]> searchProductsForChatbot(
        @Param("keyword") String keyword,
        @Param("gender") String gender,
        @Param("size") String size,
        @Param("color") String color,
        Pageable pageable
);
/**
 * Tìm sản phẩm trong một khoảng giá.
 *
 * minPrice = null: không giới hạn giá thấp nhất.
 * maxPrice = null: không giới hạn giá cao nhất.
 */
@Query("""
    select
        bt.sanPham.id,
        bt.sanPham.ten,
        min(coalesce(bt.giaSale, bt.gia)),
        sum(coalesce(bt.soLuongTon, 0)),
        sum(coalesce(bt.soLuongDaBan, 0))
    from BienTheSanPham bt
    where bt.sanPham.dangBan = true
      and bt.dangBan = true
      and bt.soLuongTon > 0

      and (
          :minPrice is null
          or coalesce(bt.giaSale, bt.gia) >= :minPrice
      )

      and (
          :maxPrice is null
          or coalesce(bt.giaSale, bt.gia) <= :maxPrice
      )

    group by bt.sanPham.id, bt.sanPham.ten
    order by
        sum(coalesce(bt.soLuongDaBan, 0)) desc,
        min(coalesce(bt.giaSale, bt.gia)) asc
""")
List<Object[]> findProductsByPriceRangeForChatbot(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
);

    /**
     * Nạp các biến thể đang bán và còn hàng cho bộ lọc chatbot.
     * Fetch join các quan hệ ManyToOne để tránh truy vấn lặp khi tổng hợp dữ liệu.
     */
    @Query("""
        select bt
        from BienTheSanPham bt
        join fetch bt.sanPham sp
        left join fetch sp.thuongHieu
        left join fetch sp.danhMuc
        left join fetch bt.kichCo
        left join fetch bt.mauSac
        where sp.dangBan = true
          and bt.dangBan = true
          and bt.soLuongTon > 0
    """)
    List<BienTheSanPham> findAvailableForChatbot();
}
