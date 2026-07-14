package com.trendfit.api.modules.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Cho phép tất cả Preflight (OPTIONS) request
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // Bỏ qua các API auth
        if (path.contains("/api/auth/")) {
            return true;
        }

        String role = request.getHeader("User-Role");

        if (role == null || role.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Thiếu thông tin xác thực (User-Role)");
            return false;
        }

        // Các path chỉ ADMIN
        if (isAdminOnlyPath(path)) {
            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Bạn không có quyền truy cập khu vực quản trị!");
                return false;
            }
        }

        return true;
    }

    private boolean isAdminOnlyPath(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("/admin/") || 
               lowerPath.contains("/api/admin");
    }
}