# NOTE TrendFit — Shop bán áo (báo cáo / demo)

## 1. Định vị sản phẩm
- Đồ án đăng ký: **shop bán áo** (áo thun, sơ mi, hoodie, polo).
- Trang chủ đã chỉnh wording:
  - "Danh mục áo nổi bật" (thay "Chọn phong cách của bạn")
  - "Gợi ý phối áo" (thay "Gợi ý phong cách")
  - Danh mục card: Áo thun / Áo sơ mi / Hoodie / Áo polo
- Khi báo cáo giáo viên: nhấn mạnh **bán áo**, blog là gợi ý **phối áo / chọn size**, không phải fashion general.

## 2. Tài khoản demo (mật khẩu: `123`)
| Tài khoản   | Vai trò   | Được làm gì |
|-------------|-----------|-------------|
| `admin`     | ADMIN     | Toàn bộ admin |
| `nhanvien`  | EMPLOYEE  | SP, danh mục, POS, **duyệt đơn hàng** |
| `khachhang` | CUSTOMER  | Mua hàng, xem đơn của mình |

- Seed tự động khi backend start (`DataInitializer`).
- Login nhận alias: admin / nhanvien / khachhang (hoặc email đã đăng ký).

## 3. Đăng nhập — lỗi đã sửa
- **Nguyên nhân thường gặp:**
  1. Backend chưa chạy → FE báo không kết nối `localhost:8080`
  2. Nút login kẹt "Đang xác thực..." vì thiếu `finally { loading = false }` → **đã sửa**
  3. Tài khoản `nhanvien`/`khachhang` trước đây không có trong DB → **đã seed**
  4. Message lỗi object không hiện được → **đã parse string/object**
- **Cách test:** chạy API Spring Boot port 8080 → FE → Login bằng `admin` / `123`.

## 4. Admin / Nhân viên duyệt đơn — lỗi đã sửa
- **Trước:** `/api/admin/orders` chỉ ADMIN (AuthInterceptor + router `requiresAdmin` + menu ẩn với NV)
  → Nhân viên vào thống kê/duyệt đơn bị **403** hoặc không thấy menu.
- **Sau:**
  - AuthInterceptor: bỏ orders khỏi `ADMIN_ONLY_PATHS`
  - Router: bỏ `meta.requiresAdmin` trên `/admin/orders`
  - AdminLayout: menu "Đơn hàng" hiện với `ADMIN` **và** `EMPLOYEE`
  - AdminOrderView: hiện lỗi rõ (401/403/network) + nút Thử lại + null-safe chi tiết SP
- **Cách test:** login `nhanvien` / `123` → sidebar **Đơn hàng** → xem list + đổi trạng thái.

## 5. Checklist trước khi demo giáo viên
1. Start backend: `cd api && ./mvnw spring-boot:run` (port 8080)
2. Start FE: `cd trendfit-ui && npm run dev`
3. Login admin → Đơn hàng / Thống kê
4. Login nhanvien → Đơn hàng (duyệt được)
5. Login khachhang hoặc đăng ký email mới → mua thử → admin duyệt

## 6. File đã chạm
- `AuthenController.java` — alias login + message rõ
- `DataInitializer.java` — seed 3 user demo
- `AuthInterceptor.java` — NV được vào orders
- `OrderAdminController.java` — comment quyền
- `LoginView.vue` — fix loading + UX lỗi
- `AdminOrderView.vue` — error banner
- `AdminLayout.vue` — menu Đơn hàng cho NV
- `router/index.js` — bỏ requiresAdmin orders
- `HomeView.vue` — copy shop bán áo
