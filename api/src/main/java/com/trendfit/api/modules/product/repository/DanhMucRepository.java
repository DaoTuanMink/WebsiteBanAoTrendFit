package com.trendfit.api.modules.product.repository;

import com.trendfit.api.modules.product.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    // Tìm danh mục theo slug để dùng ở Frontend (khi lọc sản phẩm)
    DanhMuc findBySlug(String slug);
}