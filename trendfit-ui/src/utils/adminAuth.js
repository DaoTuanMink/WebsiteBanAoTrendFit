// src/utils/adminAuth.js
//
// TẠI SAO CẦN FILE NÀY?
// Toàn bộ API "/api/admin/**" ở backend giờ đã được bảo vệ bởi AuthInterceptor
// (xem api/.../interceptor/AuthInterceptor.java) — API sẽ trả về lỗi 401/403
// nếu request KHÔNG có header "User-Role" hợp lệ (ADMIN hoặc EMPLOYEE).
//
// Có 2 cách gọi API trong dự án này:
//   1. Dùng axios instance dùng chung `api` (khai báo ở main.js, lấy qua
//      `inject('api')`) -> header được TỰ ĐỘNG gắn, không cần quan tâm gì thêm.
//   2. Dùng `fetch()` trực tiếp (ví dụ AdminPosView.vue) -> PHẢI tự gắn header
//      thủ công, dùng hàm getAuthHeaders() bên dưới.
//
// Cách dùng với fetch:
//   fetch(url, { headers: getAuthHeaders() })
//
// Cách dùng với axios "trần" (không qua instance dùng chung):
//   axios.get(url, { headers: getAuthHeaders() })

/**
 * Trả về object header chứa thông tin người đang đăng nhập (đọc từ localStorage,
 * được lưu lại lúc đăng nhập ở LoginView.vue).
 * - "User-Role": vai trò (ADMIN / EMPLOYEE) để AuthInterceptor phân quyền.
 * - "NhanVien-ID": id người đang thao tác, dùng để backend ghi log "ai đã duyệt/thay đổi".
 */
export function getAuthHeaders() {
  const userId = localStorage.getItem('user_id')
  const userRole = localStorage.getItem('user_role')

  if (!userId) {
    // Chưa đăng nhập: trả về object rỗng, để backend tự trả lỗi 401 rõ ràng
    // thay vì gửi header rác.
    return {}
  }

  return {
    'NhanVien-ID': userId,
    'User-Role': userRole,
  }
}

export default getAuthHeaders
