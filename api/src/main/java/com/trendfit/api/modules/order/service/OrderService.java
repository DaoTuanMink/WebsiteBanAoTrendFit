package com.trendfit.api.modules.order.service;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.repository.MaGiamGiaRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired private MaGiamGiaRepository maGiamGiaRepository;
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

        // 2. Lưu thông tin đơn hàng
        dh.setTenNguoiNhan(dto.getHoTen());
        dh.setSoDienThoaiGiao(dto.getSdt());
        dh.setDiaChiGiao(dto.getDiaChi());
        dh.setTongTienHang(dto.getTongTienHang());
        dh.setTienGiam(dto.getTienGiam());
        dh.setTongThanhToan(dto.getTongThanhToan());
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
        if (dto.getItems() != null) {
            for (OrderItemDTO item : dto.getItems()) {
                BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + item.getBienTheId()));

                int soLuongMua = item.getQuantity() == null ? 0 : item.getQuantity();
                int tonKhoHienTai = bt.getSoLuongTon() == null ? 0 : bt.getSoLuongTon();

                // Kiểm tra số lượng mua hợp lệ và đủ tồn kho không
                if (soLuongMua <= 0) {
                    throw new RuntimeException("Số lượng mua không hợp lệ");
                }
                if (tonKhoHienTai < soLuongMua) {
                    throw new RuntimeException("Sản phẩm " + (bt.getMaSku() != null ? bt.getMaSku() : item.getTen()) + " không đủ tồn kho!");
                }

                ChiTietDonHang ct = new ChiTietDonHang();
                ct.setDonHang(dh);
                ct.setTenSanPham(item.getTen());
                ct.setSoLuong(soLuongMua);
                ct.setDonGia(item.getGia());
                ct.setBienTheSanPham(bt);
                ct.setKichCoSize(bt.getKichCo() != null ? bt.getKichCo().getTenKichCo() : null);
                ct.setMauSac(bt.getMauSac() != null ? bt.getMauSac().getTenMau() : null);

                chiTietDonHangRepository.save(ct);

                // --- TRỪ TỒN KHO VÀ CẬP NHẬT SỐ LƯỢNG ĐÃ BÁN ---
                bt.setSoLuongTon(tonKhoHienTai - soLuongMua);
                int daBanHienTai = bt.getSoLuongDaBan() == null ? 0 : bt.getSoLuongDaBan();
                bt.setSoLuongDaBan(daBanHienTai + soLuongMua);

                bienTheRepository.save(bt);
            }
        }
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
            int tonKhoHienTai = bt.getSoLuongTon() == null ? 0 : bt.getSoLuongTon();
            int daBanHienTai = bt.getSoLuongDaBan() == null ? 0 : bt.getSoLuongDaBan();

            int tonKhoMoi = tonKhoHienTai + (soLuongMua * factor);

            if (tonKhoMoi < 0) {
                throw new RuntimeException("Sản phẩm " + ct.getTenSanPham() + " không đủ tồn kho");
            }

            int daBanMoi = daBanHienTai - (soLuongMua * factor);
            if (daBanMoi < 0) daBanMoi = 0;

            bt.setSoLuongTon(tonKhoMoi);
            bt.setSoLuongDaBan(daBanMoi);

            bienTheRepository.save(bt);
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
    
    DonHang savedOrder = donHangRepository.save(dh);

    // 2. Xử lý chi tiết và trừ tồn
    for (OrderItemDTO item : dto.getItems()) {
        BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId())
            .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));

        if (bt.getSoLuongTon() < item.getQuantity()) 
            throw new RuntimeException("Hết hàng: " + bt.getMaSku());

        // Trừ tồn kho
        bt.setSoLuongTon(bt.getSoLuongTon() - item.getQuantity());
        bienTheRepository.save(bt);

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