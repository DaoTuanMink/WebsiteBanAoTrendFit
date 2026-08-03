package com.trendfit.api.modules.order.service;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.repository.MaGiamGiaRepository;
import com.trendfit.api.modules.marketing.service.MaGiamGiaService;
import com.trendfit.api.modules.order.dto.LichSuDonHangDTO;
import com.trendfit.api.modules.order.dto.OrderItemDTO;
import com.trendfit.api.modules.order.dto.OrderRequestDTO;
import com.trendfit.api.modules.order.dto.OrderResponseDTO;
import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.entity.LichSuDonHang;
import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import com.trendfit.api.modules.order.repository.DonHangRepository;
import com.trendfit.api.modules.order.repository.LichSuDonHangRepository;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.repository.BienTheSanPhamRepository;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired private MaGiamGiaRepository maGiamGiaRepository;
    @Autowired private MaGiamGiaService maGiamGiaService;
    @Autowired private DonHangRepository donHangRepository;
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private LichSuDonHangRepository lichSuDonHangRepository;

    // Constructor (giữ nguyên)
    OrderService(MaGiamGiaRepository maGiamGiaRepository) {
        this.maGiamGiaRepository = maGiamGiaRepository;
    }

    @Transactional
    public void taoDonHang(OrderRequestDTO dto) {
        DonHang dh = new DonHang();

        // 0. BẮT BUỘC: Sinh mã đơn hàng duy nhất để tránh lỗi null cột mã đơn
        dh.setMaDonHang("HD-" + System.currentTimeMillis());

        // 1. Gán người dùng
        if (dto.getUserId() != null) {
            NguoiDung user = nguoiDungRepository.findById(dto.getUserId()).orElse(null);
            dh.setNguoiDung(user);
        } else if (dto.getCreatorId() != null) {
            NguoiDung creator = nguoiDungRepository.findById(dto.getCreatorId()).orElse(null);
            dh.setNguoiDung(creator);
        }

        // ============================================================================
        // TÍNH LẠI TOÀN BỘ SỐ TIỀN Ở SERVER - KHÔNG TIN BẤT KỲ CON SỐ NÀO TỪ CLIENT.
        //
        // TRƯỚC ĐÂY: code này lưu thẳng dto.getTongTienHang(), dto.getTienGiam(),
        // dto.getPhiVanChuyen(), dto.getTongThanhToan() do FRONTEND tự tính rồi
        // gửi lên - nghĩa là 1 khách hàng mở DevTools (F12) sửa trực tiếp request,
        // hoặc gọi thẳng API bỏ qua giao diện, có thể tự đặt "Tổng thanh toán"
        // xuống còn 1đ, hoặc tự sửa phí ship về 0, mà hệ thống vẫn chấp nhận.
        //
        // GIỜ: server tự lấy GIÁ THẬT của từng sản phẩm từ DB, tự xác thực lại
        // voucher, và tự tính phí ship theo đúng quy tắc hệ thống - hoàn toàn
        // không phụ thuộc vào những gì client gửi lên (trừ phi các con số đó
        // do server tự tính ra và gửi lại cho FE hiển thị, như QR thanh toán).
        // ============================================================================
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng!");
        }

        java.util.Map<Integer, BienTheSanPham> bienTheMap = new java.util.HashMap<>();
        BigDecimal tongTienHangThat = BigDecimal.ZERO;

        for (OrderItemDTO item : dto.getItems()) {
            BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + item.getBienTheId()));
            bienTheMap.put(item.getBienTheId(), bt);

            int soLuongMua = item.getQuantity() == null ? 0 : item.getQuantity();
            if (soLuongMua <= 0) {
                throw new RuntimeException("Số lượng mua không hợp lệ");
            }

            // Giá THẬT lấy từ DB (ưu tiên giá sale nếu có), KHÔNG dùng item.getGia()
            // do client gửi lên - tránh trường hợp bị sửa giá qua DevTools.
            BigDecimal giaThat = (bt.getGiaSale() != null && bt.getGiaSale().compareTo(BigDecimal.ZERO) > 0)
                    ? bt.getGiaSale()
                    : bt.getGia();

            tongTienHangThat = tongTienHangThat.add(giaThat.multiply(BigDecimal.valueOf(soLuongMua)));
        }

        // Xác thực lại voucher dựa trên tongTienHangThat (không tin dto.getTienGiam()).
        // Nếu mã không còn hợp lệ tại đúng thời điểm đặt hàng (vừa hết hạn/hết lượt
        // trong lúc khách điền form) thì bỏ qua giảm giá, KHÔNG chặn đơn hàng.
        BigDecimal tienGiamThat = BigDecimal.ZERO;
        if (dto.getMaVoucher() != null && !dto.getMaVoucher().isBlank()) {
            try {
                MaGiamGia voucher = maGiamGiaService.kiemTraVoucher(dto.getMaVoucher(), tongTienHangThat);
                tienGiamThat = maGiamGiaService.tinhSoTienGiam(voucher, tongTienHangThat);
            } catch (RuntimeException ignored) {
                tienGiamThat = BigDecimal.ZERO;
            }
        }

        // Phí vận chuyển do server tự tính theo VÙNG MIỀN khách chọn + giá
        // trị đơn hàng (xem tinhPhiShipGoiY) - không dùng dto.getPhiVanChuyen()
        // để tránh bị khách tự sửa xuống 0 hoặc số bất kỳ qua DevTools.
        BigDecimal phiShipThat = tinhPhiShipGoiY(dto.getTinhThanh(), tongTienHangThat);

        BigDecimal tongThanhToanThat = tongTienHangThat.subtract(tienGiamThat).add(phiShipThat);
        if (tongThanhToanThat.compareTo(BigDecimal.ZERO) < 0) {
            tongThanhToanThat = BigDecimal.ZERO;
        }

        // 2. Lưu thông tin đơn hàng - dùng số liệu SERVER TỰ TÍNH, không dùng dto
        dh.setTenNguoiNhan(dto.getHoTen());
        dh.setSoDienThoaiGiao(dto.getSdt());
        dh.setDiaChiGiao(dto.getDiaChi());
        dh.setTongTienHang(tongTienHangThat);
        dh.setPhiVanChuyen(phiShipThat);
        dh.setTienGiam(tienGiamThat);
        dh.setTongThanhToan(tongThanhToanThat);
        dh.setTrangThai("CHO_XAC_NHAN");
        dh.setPhuongThucThanhToan(dto.getPhuongThucThanhToan());

        // 3. Gán Voucher
        if (dto.getVoucherId() != null) {
            MaGiamGia voucher = maGiamGiaRepository.findById(dto.getVoucherId()).orElse(null);
            if (voucher != null) {
                dh.setMaGiamGia(voucher);
                voucher.setSoLanDaDung(voucher.getSoLanDaDung() + 1);
                maGiamGiaRepository.save(voucher);
            }
        }

        donHangRepository.save(dh);

        // 4. LƯU CHI TIẾT ĐƠN HÀNG VÀ TRỪ TỒN KHO NGAY LẬP TỨC
        for (OrderItemDTO item : dto.getItems()) {
            BienTheSanPham bt = bienTheMap.get(item.getBienTheId());
            int soLuongMua = item.getQuantity();

            BigDecimal giaThat = (bt.getGiaSale() != null && bt.getGiaSale().compareTo(BigDecimal.ZERO) > 0)
                    ? bt.getGiaSale()
                    : bt.getGia();

            ChiTietDonHang ct = new ChiTietDonHang();
            ct.setDonHang(dh);
            ct.setTenSanPham(item.getTen());
            ct.setSoLuong(soLuongMua);
            ct.setDonGia(giaThat);
            ct.setBienTheSanPham(bt);
            ct.setKichCoSize(bt.getKichCo() != null ? bt.getKichCo().getTenKichCo() : null);
            ct.setMauSac(bt.getMauSac() != null ? bt.getMauSac().getTenMau() : null);

            chiTietDonHangRepository.save(ct);

            // --- TRỪ TỒN KHO NGUYÊN TỬ (chống race condition) ---
            // TRƯỚC ĐÂY: đọc soLuongTon ra rồi kiểm tra bằng Java, sau đó
            // mới ghi lại - 2 bước tách rời này có thể bị "lách" nếu 2
            // request chạy đúng lúc cùng nhau (xem chú thích chi tiết ở
            // BienTheSanPhamRepository.truTonKhoNguyenTu). Giờ gộp thành
            // 1 câu lệnh SQL duy nhất, MySQL tự khóa dòng khi ghi.
            int soDongBiAnhHuong = bienTheRepository.truTonKhoNguyenTu(bt.getId(), soLuongMua);
            if (soDongBiAnhHuong == 0) {
                throw new RuntimeException("Sản phẩm " + (bt.getMaSku() != null ? bt.getMaSku() : item.getTen()) + " không đủ tồn kho!");
            }
        }
    }

    /**
     * Bảng tra TỈNH/THÀNH -> VÙNG MIỀN (BAC/TRUNG/NAM), dùng KHOÁ ĐÃ CHUẨN HÓA
     * (chữ thường, không dấu) để so khớp không phụ thuộc cách viết hoa/dấu
     * câu FE gửi lên. Danh sách 34 tỉnh/thành sau sáp nhập (hiệu lực từ
     * 01/07/2025) - PHẢI khớp với danhSachTinhThanh ở CheckoutView.vue, sửa
     * ở đâu phải sửa đồng bộ cả 2 nơi.
     */
    private static final Map<String, String> TINH_MIEN = Map.ofEntries(
            // Miền Bắc
            Map.entry("tuyen quang", "BAC"), Map.entry("cao bang", "BAC"),
            Map.entry("lai chau", "BAC"), Map.entry("lao cai", "BAC"),
            Map.entry("thai nguyen", "BAC"), Map.entry("dien bien", "BAC"),
            Map.entry("lang son", "BAC"), Map.entry("son la", "BAC"),
            Map.entry("phu tho", "BAC"), Map.entry("tp. ha noi", "BAC"),
            Map.entry("tp. hai phong", "BAC"), Map.entry("bac ninh", "BAC"),
            Map.entry("quang ninh", "BAC"), Map.entry("hung yen", "BAC"),
            Map.entry("ninh binh", "BAC"),
            // Miền Trung
            Map.entry("thanh hoa", "TRUNG"), Map.entry("nghe an", "TRUNG"),
            Map.entry("ha tinh", "TRUNG"), Map.entry("quang tri", "TRUNG"),
            Map.entry("tp. hue", "TRUNG"), Map.entry("tp. da nang", "TRUNG"),
            Map.entry("quang ngai", "TRUNG"), Map.entry("gia lai", "TRUNG"),
            Map.entry("dak lak", "TRUNG"), Map.entry("khanh hoa", "TRUNG"),
            Map.entry("lam dong", "TRUNG"),
            // Miền Nam
            Map.entry("dong nai", "NAM"), Map.entry("tay ninh", "NAM"),
            Map.entry("tp. ho chi minh", "NAM"), Map.entry("dong thap", "NAM"),
            Map.entry("an giang", "NAM"), Map.entry("vinh long", "NAM"),
            Map.entry("tp. can tho", "NAM"), Map.entry("ca mau", "NAM")
    );

    private static final Map<String, BigDecimal> PHI_SHIP_THEO_VUNG = Map.of(
            "BAC", BigDecimal.valueOf(20000),
            "TRUNG", BigDecimal.valueOf(30000),
            "NAM", BigDecimal.valueOf(35000)
    );

    private String boChuanHoaKhongDau(String s) {
        if (s == null) return "";
        String khongDau = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return khongDau.replaceAll("\\s+", " ");
    }

    /**
     * Tính GỢI Ý phí vận chuyển dựa trên TỈNH/THÀNH khách chọn (server tự tra
     * ra vùng miền qua bảng TINH_MIEN, KHÔNG tin trực tiếp bất kỳ giá trị
     * vùng miền nào nếu FE có gửi kèm) + giá trị đơn hàng - ĐÂY LÀ NGUỒN TÍNH
     * DUY NHẤT ĐÁNG TIN CẬY. Cùng logic với hàm goiYPhiShip() ở
     * CheckoutView.vue để khách nhìn thấy đúng số tiền sẽ áp dụng thật -
     * NẾU SAU NÀY SỬA QUY TẮC TÍNH PHÍ SHIP, PHẢI SỬA ĐỒNG BỘ CẢ 2 NƠI.
     */
    private BigDecimal tinhPhiShipGoiY(String tinhThanh, BigDecimal tongTienHang) {
        BigDecimal nguongMienPhiShip = BigDecimal.valueOf(500000);
        if (tongTienHang.compareTo(nguongMienPhiShip) >= 0) {
            return BigDecimal.ZERO;
        }
        if (tinhThanh == null || tinhThanh.isBlank()) {
            return BigDecimal.ZERO;
        }

        String mien = TINH_MIEN.get(boChuanHoaKhongDau(tinhThanh));
        if (mien == null) {
            // Tỉnh/thành lạ (không có trong danh sách 34 tỉnh) -> áp mức phí
            // xa nhất để an toàn, tránh bị lách bằng chuỗi tùy ý.
            return BigDecimal.valueOf(35000);
        }
        return PHI_SHIP_THEO_VUNG.getOrDefault(mien, BigDecimal.valueOf(35000));
    }

    @Transactional
    public void capNhatTrangThaiDonHang(Integer id, String trangThai) {
        capNhatTrangThaiDonHang(id, trangThai, null);
    }

    /**
     * Cập nhật trạng thái đơn hàng, đồng thời ghi lại lịch sử ai đã duyệt/
     * thay đổi trạng thái (yêu cầu của Thành). nguoiThucHienId là id của
     * NguoiDung (nhân viên/admin) đang đăng nhập, được FE gửi lên qua header
     * "NhanVien-ID". Nếu không xác định được (null) thì vẫn ghi log nhưng
     * để trống người thực hiện.
     */
    @Transactional
    public void capNhatTrangThaiDonHang(Integer id, String trangThai, Integer nguoiThucHienId) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String trangThaiCu = donHang.getTrangThai();

        // Không có gì thay đổi thì không cần ghi log
        if (trangThai.equals(trangThaiCu)) {
            return;
        }

        // Kho đã bị trừ NGAY LÚC ĐẶT HÀNG (xem taoDonHang()), nên khi đơn
        // chuyển sang DA_HUY (từ bất kỳ trạng thái nào KHÁC DA_HUY), phải
        // cộng trả lại tồn kho + giảm số lượng đã bán tương ứng.
        if ("DA_HUY".equals(trangThai) && !"DA_HUY".equals(trangThaiCu)) {
            xuLyTonKho(donHang, 1);
        }

        // Cập nhật trạng thái mới cho đơn hàng
        donHang.setTrangThai(trangThai);
        donHangRepository.save(donHang);

        // Ghi lại lịch sử: ai đã duyệt / đổi trạng thái, từ trạng thái nào sang trạng thái nào
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setDonHang(donHang);
        lichSu.setTrangThaiCu(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThai);

        if (nguoiThucHienId != null) {
            NguoiDung nguoiThucHien = nguoiDungRepository.findById(nguoiThucHienId).orElse(null);
            lichSu.setNguoiThucHien(nguoiThucHien);
        }

        lichSuDonHangRepository.save(lichSu);
    }

    /**
     * Trả về toàn bộ lịch sử duyệt/thay đổi trạng thái của 1 đơn hàng,
     * mới nhất hiển thị trước, kèm tên người đã thực hiện.
     */
    @Transactional(readOnly = true)
    public List<LichSuDonHangDTO> getLichSuDonHang(Integer donHangId) {
        List<LichSuDonHang> list = lichSuDonHangRepository.findByDonHang_IdOrderByNgayThayDoiDesc(donHangId);
        List<LichSuDonHangDTO> result = new ArrayList<>();

        for (LichSuDonHang ls : list) {
            LichSuDonHangDTO dto = new LichSuDonHangDTO();
            dto.setId(ls.getId());
            dto.setTrangThaiCu(ls.getTrangThaiCu());
            dto.setTrangThaiMoi(ls.getTrangThaiMoi());
            dto.setGhiChu(ls.getGhiChu());
            dto.setNgayThayDoi(ls.getNgayThayDoi());
            dto.setTenNguoiThucHien(
                    ls.getNguoiThucHien() != null ? ls.getNguoiThucHien().getHoTen() : "Hệ thống"
            );
            result.add(dto);
        }
        return result;
    }

    private void xuLyTonKho(DonHang dh, int factor) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository.findByDonHang_Id(dh.getId());

        for (ChiTietDonHang ct : chiTiets) {
            BienTheSanPham bt = ct.getBienTheSanPham();

            if (bt == null) {
                throw new RuntimeException("Không tìm thấy biến thể sản phẩm trong chi tiết đơn hàng");
            }

            int soLuongMua = ct.getSoLuong() == null ? 0 : ct.getSoLuong();

            // factor = 1: hoàn lại tồn kho (hủy đơn) -> cộng trả lại, không
            // bao giờ thất bại nên dùng hoanTonKhoNguyenTu (không cần điều
            // kiện WHERE giới hạn).
            // factor = -1: trường hợp dự phòng nếu sau này cần trừ lại kho
            // (ví dụ khôi phục 1 đơn đã hủy) -> dùng truTonKhoNguyenTu để
            // vẫn được kiểm tra đủ hàng, tránh về âm.
            if (factor > 0) {
                bienTheRepository.hoanTonKhoNguyenTu(bt.getId(), soLuongMua * factor);
            } else {
                int soDongBiAnhHuong = bienTheRepository.truTonKhoNguyenTu(bt.getId(), soLuongMua * Math.abs(factor));
                if (soDongBiAnhHuong == 0) {
                    throw new RuntimeException("Sản phẩm " + ct.getTenSanPham() + " không đủ tồn kho");
                }
            }
        }
    }

    // Các phương thức còn lại giữ nguyên
    public List<OrderResponseDTO> findAllOrdersWithDetails() {
        List<DonHang> danhSachDonHang = donHangRepository.findAll();
        List<OrderResponseDTO> ketQua = new ArrayList<>();

        for (DonHang dh : danhSachDonHang) {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setDonHang(dh);
            dto.setChiTietDonHangs(chiTietDonHangRepository.findByDonHang_Id(dh.getId()));
            ketQua.add(dto);
        }
        return ketQua;
    }

    public List<OrderResponseDTO> findOrdersWithNullUser() {
        List<DonHang> list = donHangRepository.findByNguoiDungIsNull();
        List<OrderResponseDTO> result = new ArrayList<>();

        for (DonHang dh : list) {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setDonHang(dh);
            dto.setChiTietDonHangs(chiTietDonHangRepository.findByDonHang_Id(dh.getId()));
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Integer userId) {
        List<DonHang> list = donHangRepository.findByNguoiDung_IdOrderByNgayDatDesc(userId);
        List<OrderResponseDTO> result = new ArrayList<>();

        for (DonHang dh : list) {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setDonHang(dh);
            dto.setChiTietDonHangs(chiTietDonHangRepository.findByDonHang_Id(dh.getId()));
            result.add(dto);
        }
        return result;
    }

   @Transactional
public DonHang taoDonHangTaiQuay(OrderRequestDTO dto) {
    // 1. Tạo đơn
    DonHang dh = new DonHang();
    dh.setMaDonHang("POS-" + System.currentTimeMillis());
    dh.setTenNguoiNhan(dto.getHoTen() != null ? dto.getHoTen() : "Khách lẻ");
    dh.setSoDienThoaiGiao(dto.getSdt());
    dh.setTrangThai("DA_THANH_CONG");
    dh.setPhuongThucThanhToan(dto.getPhuongThucThanhToan());
    dh.setTongTienHang(dto.getTongTienHang());
    dh.setTongThanhToan(dto.getTongThanhToan());
    // Bán tại quầy (offline): khách nhận hàng trực tiếp ngay tại cửa hàng,
    // không phát sinh vận chuyển -> LUÔN là 0, không đọc từ dto để tránh
    // trường hợp FE lỡ gửi nhầm giá trị khác 0.
    dh.setPhiVanChuyen(BigDecimal.ZERO);
    
    DonHang savedOrder = donHangRepository.save(dh);

    // 2. Xử lý chi tiết và trừ tồn
    for (OrderItemDTO item : dto.getItems()) {
        BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId())
            .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        // Trừ tồn kho NGUYÊN TỬ (chống race condition) VÀ cộng số lượng đã
        // bán trong CÙNG 1 câu lệnh SQL - xem chú thích chi tiết ở
        // BienTheSanPhamRepository.truTonKhoNguyenTu(). Trả về 0 nếu tại
        // đúng thời điểm ghi, kho không đủ (kể cả bị người khác vừa mua hết).
        int soDongBiAnhHuong = bienTheRepository.truTonKhoNguyenTu(bt.getId(), item.getQuantity());
        if (soDongBiAnhHuong == 0) {
            throw new RuntimeException("Hết hàng: " + bt.getMaSku());
        }

        // Lưu chi tiết
        ChiTietDonHang ct = new ChiTietDonHang();
        ct.setDonHang(savedOrder);
        ct.setBienTheSanPham(bt);
        ct.setSoLuong(item.getQuantity());
        ct.setDonGia(item.getGia());
        ct.setTenSanPham(item.getTen());
        ct.setKichCoSize(bt.getKichCo().getTenKichCo());
        ct.setMauSac(bt.getMauSac().getTenMau());
        chiTietDonHangRepository.save(ct);
    }
    return savedOrder;
}
}