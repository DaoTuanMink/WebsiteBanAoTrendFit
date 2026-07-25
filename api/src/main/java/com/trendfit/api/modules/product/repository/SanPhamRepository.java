package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.SanPham;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    // Tìm kiếm cơ bản phục vụ cho trang Admin (lọc theo danh mục/thương hiệu)
    List<SanPham> findByDanhMuc_IdAndThuongHieu_Id(Integer danhMucId, Integer thuongHieuId);
    List<SanPham> findByTenContainingIgnoreCase(String ten);

    /**
     * Chức năng "Gợi ý sản phẩm liên quan" (Đạt):
     * Lấy các sản phẩm đang bán, khác sản phẩm hiện tại, mà có CÙNG Danh Mục
     * HOẶC CÙNG Thương Hiệu với sản phẩm đang xem.
     * Ưu tiên sản phẩm trùng cả 2 tiêu chí lên đầu, sau đó tới đánh giá trung
     * bình cao hơn. Giới hạn số lượng bằng Pageable (ví dụ 8 sản phẩm).
     *
     * Lưu ý: dùng LEFT JOIN tường minh (thay vì path expression "sp.danhMuc.id")
     * để những sản phẩm chưa gán Danh Mục hoặc Thương Hiệu (null) không bị loại
     * khỏi kết quả một cách âm thầm do JPQL tự sinh inner join.
     */
    @Query("""
        select sp from SanPham sp
        left join sp.danhMuc dm
        left join sp.thuongHieu th
        where sp.id <> :sanPhamId
          and sp.dangBan = true
          and (
                (:danhMucId is not null and dm.id = :danhMucId)
             or (:thuongHieuId is not null and th.id = :thuongHieuId)
          )
        order by
            case when dm.id = :danhMucId and th.id = :thuongHieuId then 0 else 1 end,
            sp.danhGiaTrungBinh desc
    """)
    List<SanPham> timSanPhamLienQuan(
            @Param("sanPhamId") Integer sanPhamId,
            @Param("danhMucId") Integer danhMucId,
            @Param("thuongHieuId") Integer thuongHieuId,
            Pageable pageable
    );
}