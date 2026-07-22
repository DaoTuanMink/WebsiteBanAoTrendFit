package com.trendfit.api.modules.order.service;

import com.trendfit.api.modules.order.dto.CartSyncDTO;
import com.trendfit.api.modules.order.entity.ChiTietGioHang;
import com.trendfit.api.modules.order.entity.GioHang;
import com.trendfit.api.modules.order.repository.ChiTietGioHangRepository;
import com.trendfit.api.modules.order.repository.GioHangRepository;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.repository.BienTheSanPhamRepository;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private ChiTietGioHangRepository chiTietGioHangRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;
    @Autowired private NguoiDungRepository nguoiDungRepository;

    // 1. Lấy giỏ hàng theo User ID để trả về cho Frontend
    public List<Map<String, Object>> getCartByUserId(Integer userId) {
        GioHang cart = gioHangRepository.findByNguoiDung_Id(userId).orElse(null);
        if (cart == null) return new ArrayList<>();

        List<ChiTietGioHang> items = chiTietGioHangRepository.findByGioHang_Id(cart.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChiTietGioHang item : items) {
            BienTheSanPham bt = item.getBienTheSanPham();
            if (bt != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("bienTheId", bt.getId());
                map.put("ten", bt.getSanPham() != null ? bt.getSanPham().getTen() : "Sản phẩm");
                map.put("kichCoSize", bt.getKichCo() != null ? bt.getKichCo().getTenKichCo() : "N/A");
                map.put("mauSac", bt.getMauSac() != null ? bt.getMauSac().getTenMau() : "N/A");
                map.put("gia", bt.getGiaSale() != null ? bt.getGiaSale() : bt.getGia());
                map.put("quantity", item.getSoLuong());
                map.put("soLuongTon", bt.getSoLuongTon());
                result.add(map);
            }
        }
        return result;
    }

    // 2. Đồng bộ giỏ hàng từ Frontend lên Database mỗi khi người dùng thay đổi
    @Transactional
    public void syncCart(CartSyncDTO dto) {
        if (dto.getUserId() == null) return;

        NguoiDung user = nguoiDungRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Lấy hoặc tạo mới giỏ hàng của user
        GioHang cart = gioHangRepository.findByNguoiDung_Id(dto.getUserId())
                .orElseGet(() -> {
                    GioHang newCart = new GioHang();
                    newCart.setNguoiDung(user);
                    return gioHangRepository.save(newCart);
                });

        // Xóa các chi tiết cũ và ghi đè danh sách mới từ client gửi lên
        chiTietGioHangRepository.deleteByGioHang_Id(cart.getId());

        if (dto.getItems() != null) {
            for (CartSyncDTO.CartItemDTO itemDto : dto.getItems()) {
                BienTheSanPham bt = bienTheRepository.findById(itemDto.getBienTheId()).orElse(null);
                if (bt != null) {
                    ChiTietGioHang chiTiet = new ChiTietGioHang();
                    chiTiet.setGioHang(cart);
                    chiTiet.setBienTheSanPham(bt);
                    chiTiet.setSoLuong(itemDto.getQuantity());
                    chiTietGioHangRepository.save(chiTiet);
                }
            }
        }
    }
}