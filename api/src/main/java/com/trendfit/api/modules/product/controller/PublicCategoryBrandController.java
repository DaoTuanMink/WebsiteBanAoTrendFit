package com.trendfit.api.modules.product.controller;

import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * API CÔNG KHAI (không cần đăng nhập) trả về danh sách Danh Mục / Thương Hiệu.
 *
 * Vì sao cần file này: HomeView.vue (trang chủ) gọi
 * "GET /api/public/categories" và "GET /api/public/brands" để hiển thị,
 * nhưng trước đây 2 API này CHƯA TỪNG được tạo ở backend (chỉ có bản dành
 * cho ADMIN tại "/api/admin/products/categories" và ".../brands", yêu cầu
 * đăng nhập) — khiến trang chủ luôn báo lỗi 404 kèm CORS trong console.
 *
 * Nằm dưới "/api/public/**" nên KHÔNG bị AuthInterceptor chặn (interceptor chỉ
 * áp dụng cho "/api/admin/**"), ai cũng gọi được, phù hợp với việc hiển thị ở
 * trang chủ công khai.
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin("*")
public class PublicCategoryBrandController {

    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private ThuongHieuRepository thuongHieuRepository;

    @GetMapping("/categories")
    public Object getCategories() {
        return danhMucRepository.findAll();
    }

    @GetMapping("/brands")
    public Object getBrands() {
        return thuongHieuRepository.findAll();
    }
}
