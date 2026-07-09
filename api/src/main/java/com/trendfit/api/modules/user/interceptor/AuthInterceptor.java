package com.trendfit.api.modules.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String path = request.getRequestURI();
    // Bỏ qua các API login/register
    if (path.contains("/api/auth/")) return true;

    String role = request.getHeader("User-Role");

    // Chỉ cho ADMIN làm những việc nhạy cảm
    if (path.contains("/api/admin/orders") || path.contains("/api/admin/vouchers") || path.contains("/api/admin/staff")) {
        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Bạn không có quyền quản lý khu vực này!");
            return false;
        }
    }
    return true;
}
}