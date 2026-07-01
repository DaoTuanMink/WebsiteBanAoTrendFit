package com.trendfit.api.modules.historyOrder.service;

import com.trendfit.api.modules.historyOrder.dto.HistoryOrderDTO;
import com.trendfit.api.modules.historyOrder.repository.HistoryOrderRepository;
import com.trendfit.api.modules.order.entity.DonHang;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryOrderService {

    private final HistoryOrderRepository historyOrderRepository;

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
}