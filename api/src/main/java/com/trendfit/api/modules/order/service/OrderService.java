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
import java.util.List; // <--- Đã thêm import này

@Service
public class OrderService {
    @Autowired private MaGiamGiaRepository maGiamGiaRepository;
    @Autowired private DonHangRepository donHangRepository;
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;

    
    OrderService(MaGiamGiaRepository maGiamGiaRepository) {
        this.maGiamGiaRepository = maGiamGiaRepository;
    }

    @Transactional
    public void taoDonHang(OrderRequestDTO dto) {
        DonHang dh = new DonHang();
        
        // 1. Gán người dùng (đã fix lỗi null)
        if (dto.getUserId() != null) {
        NguoiDung user = nguoiDungRepository.findById(dto.getUserId()).orElse(null);
        dh.setNguoiDung(user); 
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
        
        // Save đơn hàng trước để lấy ID
        donHangRepository.save(dh);

        // 4. LƯU CHI TIẾT ĐƠN HÀNG (MỚI THÊM VÀO)
        if (dto.getItems() != null) {
            for (OrderItemDTO item : dto.getItems()) {
                ChiTietDonHang ct = new ChiTietDonHang();
                ct.setDonHang(dh); // Gán đơn hàng cha
                ct.setTenSanPham(item.getTen());
                ct.setSoLuong(item.getQuantity());
                ct.setDonGia(item.getGia());
                
                // Giả sử bạn lấy bienTheSanPham theo ID
                BienTheSanPham bt = bienTheRepository.findById(item.getBienTheId()).orElse(null);
                ct.setBienTheSanPham(bt);
                
                chiTietDonHangRepository.save(ct);
            }
        }
    }

    @Transactional
    public void capNhatTrangThaiDonHang(Integer id, String trangThai) {
        DonHang dh = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String trangThaiCu = dh.getTrangThai();
        
        if (trangThai.equals("DA_THANH_CONG")) {
            xuLyTonKho(dh, -1);
        } else if (trangThai.equals("DA_HUY") && !trangThaiCu.equals("DA_HUY")) {
            xuLyTonKho(dh, 1);
        }

        dh.setTrangThai(trangThai);
        donHangRepository.save(dh);
    }

    private void xuLyTonKho(DonHang dh, int factor) {
        List<ChiTietDonHang> chiTiets = chiTietDonHangRepository.findByDonHang_Id(dh.getId());
        for (ChiTietDonHang ct : chiTiets) {
            BienTheSanPham bt = ct.getBienTheSanPham();
            
            // Dùng soLuongTon thay vì soLuong
            // factor = -1 khi đơn thành công (trừ kho)
            // factor = 1 khi đơn bị hủy (cộng kho)
            bt.setSoLuongTon(bt.getSoLuongTon() + (ct.getSoLuong() * factor));
            
            // Cập nhật số lượng đã bán (nếu đơn thành công thì cộng thêm)
            if (factor == -1) {
                bt.setSoLuongDaBan(bt.getSoLuongDaBan() + ct.getSoLuong());
            } else if (factor == 1) {
                bt.setSoLuongDaBan(bt.getSoLuongDaBan() - ct.getSoLuong());
            }
            
            bienTheRepository.save(bt);
        }
    }

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

// Thêm vào OrderService.java
public List<DonHang> findDonHangByUserId(Integer userId) {
    return donHangRepository.findByNguoiDung_IdOrderByNgayDatDesc(userId);
}
}