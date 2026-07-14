package com.trendfit.api.modules.order.service;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.repository.MaGiamGiaRepository;
import com.trendfit.api.modules.order.dto.OrderItemDTO;
import com.trendfit.api.modules.order.dto.OrderRequestDTO;
import com.trendfit.api.modules.order.dto.OrderResponseDTO;
import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import com.trendfit.api.modules.order.repository.DonHangRepository;
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

    // Constructor (giữ nguyên)
    OrderService(MaGiamGiaRepository maGiamGiaRepository) {
        this.maGiamGiaRepository = maGiamGiaRepository;
    }

    @Transactional
    public void taoDonHang(OrderRequestDTO dto) {
        DonHang dh = new DonHang();

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

        // 4. LƯU CHI TIẾT ĐƠN HÀNG
        if (dto.getItems() != null) {
            for (OrderItemDTO item : dto.getItems()) {
                ChiTietDonHang ct = new ChiTietDonHang();
                ct.setDonHang(dh);
                ct.setTenSanPham(item.getTen());
                ct.setSoLuong(item.getQuantity());
                ct.setDonGia(item.getGia());

                BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId()).orElse(null);
                if (bt != null) {
                    ct.setBienTheSanPham(bt);
                    // SỬA Ở ĐÂY - Lấy từ entity
                    ct.setKichCoSize(bt.getKichCo() != null ? bt.getKichCo().getTenKichCo() : null);  // ← SỬA
    ct.setMauSac(bt.getMauSac() != null ? bt.getMauSac().getTenMau() : null);
                }

                chiTietDonHangRepository.save(ct);
            }
        }
    }

    @Transactional
    public void capNhatTrangThaiDonHang(Integer id, String trangThai) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String trangThaiCu = donHang.getTrangThai();

        if ("DA_THANH_CONG".equals(trangThai) && !"DA_THANH_CONG".equals(trangThaiCu)) {
            xuLyTonKho(donHang, -1);
        }

        if ("DA_HUY".equals(trangThai) && "DA_THANH_CONG".equals(trangThaiCu)) {
            xuLyTonKho(donHang, 1);
        }

        donHang.setTrangThai(trangThai);
        donHangRepository.save(donHang);
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
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        DonHang donHang = new DonHang();

        donHang.setTenNguoiNhan(dto.getHoTen() == null || dto.getHoTen().trim().isEmpty() ? "Khách lẻ" : dto.getHoTen().trim());
        donHang.setSoDienThoaiGiao(dto.getSdt());
        donHang.setDiaChiGiao("Bán tại quầy");
        donHang.setPhiVanChuyen(BigDecimal.ZERO);
        donHang.setTienGiam(dto.getTienGiam() == null ? BigDecimal.ZERO : dto.getTienGiam());
        donHang.setTongTienHang(BigDecimal.ZERO);
        donHang.setTongThanhToan(BigDecimal.ZERO);
        donHang.setTrangThai("DA_THANH_CONG");
        donHang.setPhuongThucThanhToan(dto.getPhuongThucThanhToan() == null ? "TIEN_MAT" : dto.getPhuongThucThanhToan());
        donHang.setGhiChu("Đơn bán tại quầy");
        donHang.setNgayDat(LocalDateTime.now());

        DonHang savedOrder = donHangRepository.save(donHang);
        BigDecimal tongTienHang = BigDecimal.ZERO;

        for (OrderItemDTO item : dto.getItems()) {
            BienTheSanPham bienThe = bienTheRepository.findById(item.getBienTheId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            int soLuongMua = item.getQuantity() == null ? 0 : item.getQuantity();
            int tonKho = bienThe.getSoLuongTon() == null ? 0 : bienThe.getSoLuongTon();

            if (soLuongMua <= 0) throw new RuntimeException("Số lượng mua không hợp lệ");
            if (tonKho < soLuongMua) throw new RuntimeException("Sản phẩm không đủ tồn kho");

            BigDecimal donGia = item.getGia() != null ? item.getGia() :
                    (bienThe.getGiaSale() != null ? bienThe.getGiaSale() : bienThe.getGia());

            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuongMua));

            ChiTietDonHang chiTiet = new ChiTietDonHang();
            chiTiet.setDonHang(savedOrder);
            chiTiet.setBienTheSanPham(bienThe);
            chiTiet.setSoLuong(soLuongMua);
            chiTiet.setDonGia(donGia);
            chiTiet.setThanhTien(thanhTien);
            chiTiet.setTenSanPham(item.getTen());
            chiTiet.setMaSku(bienThe.getMaSku());

            // SỬA Ở ĐÂY
            chiTiet.setKichCoSize(bienThe.getKichCo() != null ? bienThe.getKichCo().getTenKichCo() : null);  // ← SỬA
chiTiet.setMauSac(bienThe.getMauSac() != null ? bienThe.getMauSac().getTenMau() : null);

            chiTietDonHangRepository.save(chiTiet);

            bienThe.setSoLuongTon(tonKho - soLuongMua);
            bienThe.setSoLuongDaBan((bienThe.getSoLuongDaBan() == null ? 0 : bienThe.getSoLuongDaBan()) + soLuongMua);
            bienTheRepository.save(bienThe);

            tongTienHang = tongTienHang.add(thanhTien);
        }

        // Phần tính tiền thanh toán giữ nguyên
        BigDecimal tienGiam = dto.getTienGiam() == null ? BigDecimal.ZERO : dto.getTienGiam();
        BigDecimal tongThanhToan = tongTienHang.subtract(tienGiam);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;

        BigDecimal tienKhachDua = dto.getTienKhachDua() == null ? BigDecimal.ZERO : dto.getTienKhachDua();
        String phuongThuc = savedOrder.getPhuongThucThanhToan();

        if ("TIEN_MAT".equals(phuongThuc)) {
            if (tienKhachDua.compareTo(tongThanhToan) < 0) {
                throw new RuntimeException("Tiền khách đưa chưa đủ");
            }
            savedOrder.setTienKhachDua(tienKhachDua);
            savedOrder.setTienThua(tienKhachDua.subtract(tongThanhToan));
        } else {
            savedOrder.setTienKhachDua(tongThanhToan);
            savedOrder.setTienThua(BigDecimal.ZERO);
        }

        savedOrder.setTongTienHang(tongTienHang);
        savedOrder.setTienGiam(tienGiam);
        savedOrder.setTongThanhToan(tongThanhToan);

        return donHangRepository.save(savedOrder);
    }
}