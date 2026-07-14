package com.trendfit.api.modules.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().toLowerCase();
        String method = request.getMethod();

        // Cho phép Preflight (OPTIONS)
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // Bỏ qua API auth
        if (path.contains("/api/auth/")) {
            return true;
        }

        // ====================== CHỈ 3 CHỨC NĂNG ADMIN ======================
        if (isAdminOnlyPath(path)) {
            String role = request.getHeader("User-Role");

            if (role == null || role.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Thiếu thông tin xác thực (User-Role)");
                return false;
            }

            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Bạn không có quyền truy cập khu vực quản trị!");
                return false;
            }
        }

        // Các API khác: ai cũng vào được (khách, user, admin)
        return true;
    }

    /**
     * Chỉ 3 chức năng này là ADMIN mới được dùng
     */
    private boolean isAdminOnlyPath(String path) {
        return path.contains("/api/admin/orders") ||
               path.contains("/api/admin/vouchers") ||
               path.contains("/api/admin/staff") ||
               path.contains("/api/admin/employees"); // nếu có
    }
}