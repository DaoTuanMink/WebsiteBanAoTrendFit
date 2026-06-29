package com.trendfit.api.modules.user.interceptor;

import com.trendfit.api.modules.user.repository.PhanQuyenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired private PhanQuyenRepository phanQuyenRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Lấy ID nhân viên từ header (hoặc token)
        String nhanVienIdStr = request.getHeader("NhanVien-ID");
        if (nhanVienIdStr == null) return true; // Nếu là Admin hoặc khách thì bỏ qua

        Integer nhanVienId = Integer.parseInt(nhanVienIdStr);
        String method = request.getMethod(); // GET, POST, PUT, DELETE
        String module = "DON_HANG"; // Tạm thời để cứng, bạn có thể lấy từ path

        // 2. Kiểm tra quyền dựa trên phương thức
        boolean hasPermission = false;
        if (method.equals("PUT")) {
            hasPermission = phanQuyenRepository.existsByNhanVienIdAndModuleAndSuaDuocTrue(nhanVienId, module);
        } else if (method.equals("POST")) {
            hasPermission = phanQuyenRepository.existsByNhanVienIdAndModuleAndThemDuocTrue(nhanVienId, module);
        }

        if (!hasPermission) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Bạn không có quyền thực hiện thao tác này!");
            return false;
        }
        return true;
    }
}