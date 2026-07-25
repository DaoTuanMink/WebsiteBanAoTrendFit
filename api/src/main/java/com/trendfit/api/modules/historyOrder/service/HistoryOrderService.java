package com.trendfit.api.modules.historyOrder.service;

import com.trendfit.api.modules.historyOrder.dto.CustomerActivitySummaryDTO;
import com.trendfit.api.modules.historyOrder.dto.HistoryOrderDTO;
import com.trendfit.api.modules.historyOrder.dto.TopProductDTO;
import com.trendfit.api.modules.historyOrder.repository.HistoryOrderRepository;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryOrderService {

    private final HistoryOrderRepository historyOrderRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public List<HistoryOrderDTO> getHistory(Integer userId) {

    List<DonHang> donHangs =
            historyOrderRepository.findByNguoiDung_IdAndTrangThaiOrderByNgayDatDesc(
                    userId,
                    "DA_THANH_CONG"
            );

    return donHangs.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
}

    private HistoryOrderDTO toDTO(DonHang order) {

        HistoryOrderDTO dto = new HistoryOrderDTO();

        dto.setId(order.getId());
        dto.setMaDonHang(order.getMaDonHang());
        dto.setNgayDat(order.getNgayDat());
        dto.setTongThanhToan(order.getTongThanhToan());
        dto.setTrangThai(order.getTrangThai());
        dto.setPhuongThucThanhToan(order.getPhuongThucThanhToan());
        dto.setTenNguoiNhan(order.getTenNguoiNhan());

        return dto;
    }

    /**
     * Trả về lịch sử hoạt động mua sắm tổng hợp của khách hàng (yêu cầu Việt):
     * - Tổng số đơn hàng đã mua thành công
     * - Tổng số tiền đã bỏ ra (cộng dồn tongThanhToan các đơn DA_THANH_CONG)
     * - Top 5 sản phẩm mua nhiều nhất
     */
    public CustomerActivitySummaryDTO getSummary(Integer userId) {
        List<DonHang> donHangsThanhCong =
                historyOrderRepository.findByNguoiDung_IdAndTrangThaiOrderByNgayDatDesc(
                        userId,
                        "DA_THANH_CONG"
                );

        BigDecimal tongTienDaChi = donHangsThanhCong.stream()
                .map(DonHang::getTongThanhToan)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pageable top5 = PageRequest.of(0, 5);
        List<Object[]> rows = chiTietDonHangRepository.thongKeSanPhamMuaNhieuNhat(userId, top5);

        List<TopProductDTO> topProducts = new ArrayList<>();
        for (Object[] row : rows) {
            Integer sanPhamId = (Integer) row[0];
            String tenSanPham = (String) row[1];
            Long soLuong = ((Number) row[2]).longValue();
            BigDecimal tongTien = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            topProducts.add(new TopProductDTO(sanPhamId, tenSanPham, soLuong, tongTien));
        }

        CustomerActivitySummaryDTO summary = new CustomerActivitySummaryDTO();
        summary.setTongSoDonHang(donHangsThanhCong.size());
        summary.setTongTienDaChi(tongTienDaChi);
        summary.setSanPhamMuaNhieuNhat(topProducts);
        return summary;
    }
}