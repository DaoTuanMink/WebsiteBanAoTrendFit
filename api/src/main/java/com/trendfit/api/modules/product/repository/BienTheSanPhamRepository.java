package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.BienTheSanPham;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    // Tìm theo sản phẩm cha
    List<BienTheSanPham> findBySanPham_Id(Integer sanPhamId);
    
    @Modifying
    @Transactional
    void deleteBySanPham_Id(Integer sanPhamId);

    /**
     * TRỪ TỒN KHO NGUYÊN TỬ (atomic) - khắc phục race condition khi 2 người
     * mua cùng lúc sản phẩm sắp hết hàng.
     *
     * TRƯỚC ĐÂY: code đọc soLuongTon ra (SELECT), kiểm tra đủ hàng bằng Java
     * (if...), rồi mới ghi lại (UPDATE) qua 2 câu lệnh SQL riêng biệt. Nếu 2
     * request cùng đọc thấy "còn 1" TRƯỚC KHI cả 2 kịp ghi, cả 2 đều vượt
     * qua được điều kiện kiểm tra và cùng trừ đi 1 -> tồn kho có thể bị ÂM.
     *
     * GIỜ: gộp "kiểm tra + trừ" thành DUY NHẤT 1 câu lệnh SQL, với điều kiện
     * "so_luong_ton >= :soLuong" nằm ngay trong WHERE. MySQL tự động khóa
     * dòng (row lock) trong lúc thực thi UPDATE, nên nếu 2 request cùng chạy
     * đồng thời, request thứ 2 BẮT BUỘC phải đợi request thứ 1 ghi xong rồi
     * mới đọc số liệu MỚI NHẤT - không còn cửa để cả 2 cùng "lách" qua được.
     *
     * @return số dòng bị ảnh hưởng: 1 = trừ thành công, 0 = KHÔNG đủ tồn kho
     *         tại đúng thời điểm này (kể cả trường hợp bị request khác vừa
     *         trừ mất ngay trước đó) -> Service phải ném lỗi "Hết hàng!".
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
    int truTonKhoNguyenTu(@Param("id") Integer id, @Param("soLuong") int soLuong);

    /**
     * HOÀN TỒN KHO NGUYÊN TỬ - dùng khi hủy đơn hàng (cộng trả lại số lượng
     * đã trừ trước đó). Không cần điều kiện WHERE giới hạn như hàm trừ ở
     * trên vì cộng thêm luôn hợp lệ, nhưng vẫn giữ nguyên tắc 1 câu lệnh SQL
     * duy nhất để tránh đọc-rồi-ghi tách rời.
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
    int hoanTonKhoNguyenTu(@Param("id") Integer id, @Param("soLuong") int soLuong);
}