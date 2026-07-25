package com.trendfit.api.modules.user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ============================================================================
 *  BỘ LỌC PHÂN QUYỀN (AUTHORIZATION) CHO TOÀN BỘ API "/api/admin/**"
 * ============================================================================
 * Vì dự án KHÔNG dùng Spring Security/JWT mà chỉ đăng nhập đơn giản qua
 * localStorage, nên đây là nơi DUY NHẤT chặn người không có quyền gọi thẳng
 * vào API quản trị (ví dụ dùng Postman/console gọi thẳng, bỏ qua giao diện).
 *
 * CÁCH HOẠT ĐỘNG:
 *  - Phía Frontend (xem "src/main.js"), sau khi đăng nhập, mỗi request gọi
 *    qua axios instance dùng chung `api` sẽ TỰ ĐỘNG gắn 2 header:
 *      + "User-Role"   : vai trò của người đang đăng nhập (ADMIN / EMPLOYEE / CUSTOMER)
 *      + "NhanVien-ID"  : id của người đang đăng nhập (dùng để ghi log "ai đã thao tác")
 *  - Interceptor này đọc header "User-Role" để quyết định cho qua hay chặn.
 *
 * QUY TẮC PHÂN QUYỀN (đơn giản, 2 cấp):
 *  1) Nhóm "ADMIN_ONLY"  : chỉ Quản trị viên (ADMIN) mới được dùng.
 *     Gồm: quản lý đơn hàng (duyệt/hủy), quản lý nhân viên, quản lý voucher/khuyến mãi.
 *     Lý do: đây là các nghiệp vụ ảnh hưởng tiền bạc / nhân sự, rủi ro cao.
 *  2) Nhóm "STAFF_AREA"  : ADMIN và EMPLOYEE (nhân viên) đều được dùng.
 *     Gồm: quản lý sản phẩm/danh mục/thương hiệu/size-màu, bán hàng tại quầy,
 *     xem thống kê doanh số.
 *     Lý do: đây là công việc vận hành hàng ngày mà nhân viên bán hàng cũng cần làm.
 *
 * LƯU Ý CHO THÀNH VIÊN KHÁC:
 *  - Nếu bạn thêm 1 Controller mới nằm trong "/api/admin/...", API đó sẽ TỰ ĐỘNG
 *    rơi vào nhóm STAFF_AREA (yêu cầu tối thiểu là EMPLOYEE) trừ khi bạn thêm
 *    đường dẫn đó vào danh sách ADMIN_ONLY_PATHS bên dưới.
 *  - Nếu 1 API admin không tự gắn header (gọi bằng axios "trần" thay vì
 *    dùng instance `api` trong main.js) thì sẽ luôn bị chặn 401 — hãy sửa lại
 *    phía frontend dùng `inject('api')` thay vì `import axios from 'axios'`.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * Các tiền tố đường dẫn CHỈ dành riêng cho ADMIN (quản trị viên cấp cao).
     * Nhân viên (EMPLOYEE) gọi vào các API này sẽ bị từ chối (403).
     */
    private static final String[] ADMIN_ONLY_PATHS = {
            "/api/admin/orders",   // Duyệt / đổi trạng thái / xem lịch sử duyệt đơn hàng
            "/api/admin/users",    // Quản lý tài khoản nhân viên (tạo/sửa/xóa)
            "/api/admin/vouchers", // Quản lý mã giảm giá
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().toLowerCase();
        String method = request.getMethod();

        // 1. Luôn cho phép request "preflight" CORS (trình duyệt tự gửi trước mỗi request thật)
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // 2. Các API xác thực (đăng nhập/đăng ký) phải luôn công khai, không được chặn ở đây
        if (path.contains("/api/auth/")) {
            return true;
        }

        // 3. Chỉ những API nằm dưới "/api/admin/" mới cần kiểm tra quyền.
        //    Các API "/api/public/...", giỏ hàng, đặt hàng của khách... không đụng tới.
        if (!path.contains("/api/admin/")) {
            return true;
        }

        String role = request.getHeader("User-Role");

        // 4. Không có header User-Role -> chưa đăng nhập hoặc gọi thẳng API, không được vào
        if (role == null || role.isBlank()) {
            tuChoi(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Thiếu thông tin xác thực (User-Role). Vui lòng đăng nhập lại.");
            return false;
        }

        boolean laAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean laNhanVien = "EMPLOYEE".equalsIgnoreCase(role);

        // 5. Không phải ADMIN cũng không phải EMPLOYEE (ví dụ khách hàng CUSTOMER) -> chặn hết
        if (!laAdmin && !laNhanVien) {
            tuChoi(response, HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập khu vực quản trị!");
            return false;
        }

        // 6. Là EMPLOYEE nhưng cố truy cập vùng ADMIN_ONLY -> chặn, TRỪ 2 ngoại lệ
        //    liên quan tới voucher, vì màn hình "Bán hàng tại quầy" cần EMPLOYEE
        //    gợi ý & áp mã giảm giá cho khách (không được TẠO/SỬA/XÓA voucher):
        //      a) GET  /api/admin/vouchers        -> xem danh sách voucher đang hoạt động
        //      b) POST /api/admin/vouchers/check  -> kiểm tra 1 mã có hợp lệ không (không ghi dữ liệu)
        if (laNhanVien && isAdminOnlyPath(path)) {
            boolean laXemDanhSachVoucher =
                    path.endsWith("/api/admin/vouchers") && "GET".equalsIgnoreCase(method);
            boolean laKiemTraVoucher =
                    path.contains("/api/admin/vouchers/check") && "POST".equalsIgnoreCase(method);

            if (!laXemDanhSachVoucher && !laKiemTraVoucher) {
                tuChoi(response, HttpServletResponse.SC_FORBIDDEN,
                        "Chức năng này chỉ dành cho Quản trị viên (ADMIN)!");
                return false;
            }
        }

        // 7. Hợp lệ: ADMIN vào được tất cả; EMPLOYEE vào được các API không nằm trong ADMIN_ONLY_PATHS
        return true;
    }

    private boolean isAdminOnlyPath(String path) {
        for (String prefix : ADMIN_ONLY_PATHS) {
            if (path.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void tuChoi(HttpServletResponse response, int statusCode, String message) throws Exception {
        response.setStatus(statusCode);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(message);
    }
}
