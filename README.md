1. Mục tiêu
Xây dựng website bán áo thời trang TrendFit. Tập trung vào luồng mua hàng online, quản lý kho biến thể (Size/Màu) và duyệt đơn hàng.

2. Cấu trúc Entity Cốt lõi (Đã được làm chuẩn để Pass đồ án)
Sản phẩm: SanPham, BienTheSanPham (có kichCoSize, mauSac, soLuongTon), AnhSanPham.

Đơn hàng: DonHang (trường quan trọng: trangThai, tongThanhToan), ChiTietDonHang.

Log trạng thái (Mới thêm): LichSuDonHang (Lưu log thay đổi trạng thái đơn hàng).

Người dùng: NguoiDung, DiaChi.

Marketing: MaGiamGia, Banner.

3. Phân chia công việc (Quy trình làm việc độc lập)
Minh (Tôi): Phụ trách Client (Trang chủ, Trang Chi Tiết, Giỏ hàng, Checkout, Logic đặt hàng CHO_XAC_NHAN).

Member 2 (Sản phẩm): Quản lý Admin, CRUD SanPham & BienTheSanPham.

Member 3 (Đơn hàng): Quản lý OrderAdminController, duyệt trạng thái đơn, update LichSuDonHang, trừ kho tồn.

Member 4 (User/Auth): Đăng nhập, JWT Token, bảo mật SecurityConfig.

Member 5 (Marketing): Quản lý MaGiamGia.

Member 6 (Thống kê): Dashboard doanh thu.

4. API Luồng đặt hàng quan trọng
Đặt hàng: POST /api/public/orders/place (Gửi JSON đơn hàng).

Duyệt đơn: PUT /api/admin/orders/{id}/status?newStatus=... (Cập nhật trạng thái + Ghi log LichSuDonHang).

Dữ liệu danh mục áo: GET /api/public/products (Hỗ trợ filter).

5. Quy tắc Git
Nhánh main đã bị khóa (Protected branch), bắt buộc tạo Pull Request và chỉ Minh (Admin) mới có quyền Merge.

Tên nhánh: feature/ten-thanh-vien-tinh-nang.
