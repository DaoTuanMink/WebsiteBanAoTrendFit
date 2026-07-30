package com.trendfit.api.modules.order.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trendfit.api.modules.order.dto.*;
import com.trendfit.api.modules.order.entity.HoaDonCho;
import com.trendfit.api.modules.order.repository.HoaDonChoRepository;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý nghiệp vụ "Hóa đơn chờ" (đơn tạm) cho màn hình Bán hàng tại quầy.
 * Xem thêm chú thích tổng quan ở entity HoaDonCho.java.
 */
@Service
public class HoaDonChoService {

    @Autowired private HoaDonChoRepository hoaDonChoRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;

    // Dùng để chuyển đổi List<HoaDonChoItemDTO> <-> chuỗi JSON lưu trong DB.
    // Đây là cách đơn giản để "treo" cả giỏ hàng mà không cần thêm bảng chi
    // tiết riêng (xem giải thích trong HoaDonCho.java).
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lưu tạm giỏ hàng hiện tại thành 1 "Hóa đơn chờ" mới.
     * @param nguoiTaoId id của thu ngân (NhanVien-ID) đang thao tác, có thể null
     *                   nếu không xác định được (vẫn cho lưu, chỉ là không biết "của ai").
     */
    @Transactional
    public HoaDonChoDTO luuTam(HoaDonChoRequestDTO dto, Integer nguoiTaoId) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống, không có gì để lưu tạm!");
        }

        HoaDonCho hd = new HoaDonCho();
        hd.setTenKhachHang(dto.getTenKhachHang());
        hd.setSoDienThoai(dto.getSoDienThoai());
        hd.setPhuongThucThanhToan(dto.getPhuongThucThanhToan());
        hd.setMaVoucher(dto.getMaVoucher());
        hd.setVoucherId(dto.getVoucherId());
        hd.setGhiChu(dto.getGhiChu());

        // Tính sẵn tổng số lượng + tổng tiền để hiển thị nhanh ở danh sách,
        // không phải parse lại JSON mỗi lần load trang.
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;
        for (HoaDonChoItemDTO item : dto.getItems()) {
            int soLuong = item.getQuantity() != null ? item.getQuantity() : 0;
            BigDecimal gia = item.getGia() != null ? item.getGia() : BigDecimal.ZERO;
            tongSoLuong += soLuong;
            tongTien = tongTien.add(gia.multiply(BigDecimal.valueOf(soLuong)));
        }
        hd.setSoLuongSanPham(tongSoLuong);
        hd.setTongTien(tongTien);

        try {
            hd.setGioHangJson(objectMapper.writeValueAsString(dto.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Không thể lưu giỏ hàng (lỗi chuyển đổi dữ liệu): " + e.getMessage());
        }

        if (nguoiTaoId != null) {
            NguoiDung nguoiTao = nguoiDungRepository.findById(nguoiTaoId).orElse(null);
            hd.setNguoiTao(nguoiTao);
        }

        hoaDonChoRepository.save(hd);
        return toDTO(hd);
    }

    /**
     * Lấy danh sách toàn bộ hóa đơn chờ hiện có, để hiển thị ở tab
     * "Hóa đơn chờ" trên màn hình POS.
     */
    public List<HoaDonChoDTO> layDanhSach() {
        List<HoaDonChoDTO> result = new ArrayList<>();
        for (HoaDonCho hd : hoaDonChoRepository.findAllByOrderByNgayTaoDesc()) {
            result.add(toDTO(hd));
        }
        return result;
    }

    /**
     * Lấy chi tiết đầy đủ 1 hóa đơn chờ (kèm danh sách sản phẩm) để FE khôi
     * phục lại giỏ hàng khi thu ngân bấm "Gọi lại".
     */
    public HoaDonChoDetailDTO layChiTiet(Integer id) {
        HoaDonCho hd = hoaDonChoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hóa đơn chờ này không còn tồn tại (có thể đã bị gọi lại hoặc xóa)!"));

        HoaDonChoDetailDTO dto = new HoaDonChoDetailDTO();
        dto.setId(hd.getId());
        dto.setTenKhachHang(hd.getTenKhachHang());
        dto.setSoDienThoai(hd.getSoDienThoai());
        dto.setPhuongThucThanhToan(hd.getPhuongThucThanhToan());
        dto.setMaVoucher(hd.getMaVoucher());
        dto.setVoucherId(hd.getVoucherId());
        dto.setGhiChu(hd.getGhiChu());

        try {
            List<HoaDonChoItemDTO> items = objectMapper.readValue(
                    hd.getGioHangJson(),
                    new TypeReference<List<HoaDonChoItemDTO>>() {}
            );
            dto.setItems(items);
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được dữ liệu giỏ hàng đã lưu: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Xóa 1 hóa đơn chờ - dùng khi thu ngân đã "Gọi lại" xong (không cần giữ
     * bản nháp nữa) hoặc khi chủ động hủy bỏ đơn tạm này.
     */
    @Transactional
    public void xoa(Integer id) {
        if (!hoaDonChoRepository.existsById(id)) {
            throw new IllegalArgumentException("Hóa đơn chờ này không còn tồn tại!");
        }
        hoaDonChoRepository.deleteById(id);
    }

    private HoaDonChoDTO toDTO(HoaDonCho hd) {
        HoaDonChoDTO dto = new HoaDonChoDTO();
        dto.setId(hd.getId());
        dto.setTenKhachHang(hd.getTenKhachHang());
        dto.setSoDienThoai(hd.getSoDienThoai());
        dto.setSoLuongSanPham(hd.getSoLuongSanPham());
        dto.setTongTien(hd.getTongTien());
        dto.setGhiChu(hd.getGhiChu());
        dto.setTenNguoiTao(hd.getNguoiTao() != null ? hd.getNguoiTao().getHoTen() : "Không rõ");
        dto.setNgayTao(hd.getNgayTao());
        return dto;
    }
}
