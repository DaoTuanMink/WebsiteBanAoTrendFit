package com.trendfit.api.config;

import com.trendfit.api.modules.user.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình các thành phần chung của Spring MVC.
 * Hiện tại dùng để đăng ký {@link AuthInterceptor} — bộ lọc kiểm tra quyền
 * truy cập cho khu vực quản trị.
 *
 * QUAN TRỌNG: trước đây interceptor chỉ được áp dụng cho "/api/admin/orders/**",
 * khiến toàn bộ các API quản trị khác (sản phẩm, danh mục, thương hiệu,
 * nhân viên, voucher, thống kê...) hoàn toàn KHÔNG được kiểm tra quyền — bất
 * kỳ ai (kể cả chưa đăng nhập) gọi thẳng API cũng thao tác được. Đã sửa lại
 * để áp dụng cho toàn bộ "/api/admin/**", logic phân quyền chi tiết (ADMIN
 * hay EMPLOYEE) được xử lý bên trong AuthInterceptor.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Áp dụng cho TOÀN BỘ khu vực quản trị, không chỉ riêng đơn hàng nữa
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
