package com.trendfit.api.modules.order.entity;

import com.trendfit.api.modules.user.entity.NguoiDung;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ============================================================================
 *  "HÓA ĐƠN CHỜ" (còn gọi là đơn tạm / treo giỏ hàng) - dùng ở màn hình
 *  "Bán hàng tại quầy" (POS).
 * ============================================================================
 * TÁC DỤNG THỰC TẾ: khi khách đang chọn đồ mà phải đi thử size khác, hoặc
 * đang kiểm tra lại tiền... trong khi phía sau có khách khác đang chờ, thu
 * ngân bấm "Lưu tạm" để cất giỏ hàng hiện tại sang một chỗ, màn hình POS
 * trống ngay để phục vụ khách tiếp theo. Lúc khách cũ quay lại, thu ngân bấm
 * "Gọi lại" đúng hóa đơn đó để tiếp tục thanh toán mà KHÔNG cần chọn lại sản
 * phẩm từ đầu.
 *
 * KHÁC VỚI ĐƠN HÀNG THẬT (DonHang):
 * - DonHang là đơn đã hoàn tất/đang xử lý thật sự, có trừ tồn kho, có mã đơn,
 *   được tính vào doanh thu, lịch sử... (xem OrderService.taoDonHangTaiQuay).
 * - HoaDonCho chỉ là "bản nháp" tạm thời, CHƯA trừ tồn kho, CHƯA tính doanh
 *   thu. Nó biến mất (bị xóa) ngay khi được gọi lại để thanh toán, hoặc khi
 *   thu ngân chủ động hủy.
 *
 * CÁCH LƯU GIỎ HÀNG: thay vì tạo hẳn 1 bảng chi tiết riêng (nhiều cột, nhiều
 * bảng liên kết) cho một tính năng chỉ mang tính "nháp tạm thời", ta lưu
 * toàn bộ danh sách sản phẩm trong giỏ dưới dạng 1 chuỗi JSON (gioHangJson).
 * Khi cần hiển thị lại, backend/FE tự parse JSON đó ra danh sách sản phẩm.
 * Cách này đơn giản, đủ dùng, và không ảnh hưởng tới các bảng nghiệp vụ
 * chính (san_pham, bien_the_san_pham, don_hang...).
 */
@Entity
@Table(name = "hoa_don_cho")
@Data
public class HoaDonCho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Thông tin khách hàng nhập tạm lúc lưu (có thể để trống nếu là khách lẻ)
    private String tenKhachHang;
    private String soDienThoai;

    // Phương thức thanh toán dự kiến đã chọn trước khi tạm lưu (TIEN_MAT / CHUYEN_KHOAN)
    private String phuongThucThanhToan;

    // Mã voucher đã áp dụng trước khi tạm lưu (nếu có), để khi gọi lại không cần chọn lại
    private String maVoucher;
    private Integer voucherId;

    // Toàn bộ giỏ hàng tại thời điểm lưu, dạng JSON, ví dụ:
    // [{"bienTheId":12,"ten":"Áo thun nam","tenKichCo":"L","tenMau":"Đen","quantity":2,"gia":150000,"soLuongTon":10}]
    @Lob
    @Column(name = "gio_hang_json", columnDefinition = "TEXT")
    private String gioHangJson;

    // 2 cột này được tính sẵn lúc lưu, để hiển thị nhanh ở danh sách "Hóa đơn
    // chờ" mà KHÔNG cần parse lại JSON mỗi lần load danh sách.
    private Integer soLuongSanPham;
    private BigDecimal tongTien;

    // Ghi chú tự do của thu ngân (ví dụ: "khách đi thử size M")
    private String ghiChu;

    // Nhân viên/Admin đã tạo hóa đơn chờ này (để biết "của ai" nếu quầy có nhiều thu ngân)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
