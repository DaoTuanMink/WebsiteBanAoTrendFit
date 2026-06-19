package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Logic 1: Phục vụ lọc nâng cao cho Admin
    public List<SanPham> timKiemSanPhamAdmin(String search, Integer danhMucId, Integer thuongHieuId) {
        if (danhMucId != null && thuongHieuId != null) {
            return sanPhamRepository.findByTenContainingIgnoreCaseAndDanhMucIdAndThuongHieuId(search, danhMucId, thuongHieuId);
        } else if (danhMucId != null) {
            return sanPhamRepository.findByTenContainingIgnoreCaseAndDanhMucId(search, danhMucId);
        } else if (thuongHieuId != null) {
            return sanPhamRepository.findByTenContainingIgnoreCaseAndThuongHieuId(search, thuongHieuId);
        }
        return sanPhamRepository.findByTenContainingIgnoreCase(search);
    }

    // Logic 2: Phục vụ hiển thị trang chủ của Client công khai
    public List<SanPham> layTatCaSanPhamChoTrangChu() {
        return sanPhamRepository.findAll();
    }

    public SanPham layChiTietSanPham(Integer id) {
        return sanPhamRepository.findById(id).orElse(null);
    }

    public List<BienTheSanPham> layBienTheCuaSanPham(Integer id) {
        // Tạm thời trả về mảng rỗng để không bị lỗi compile, các bạn thành viên nhóm có thể nối tiếp Repository biến thể vào đây
        return new ArrayList<>();
    }

    @Transactional
    public SanPham themMoiSanPham(SanPham sanPham) {
        if (sanPham.getSlug() == null || sanPham.getSlug().trim().isEmpty()) {
            sanPham.setSlug("trendfit-" + UUID.randomUUID().toString().substring(0, 8));
        }
        return sanPhamRepository.save(sanPham);
    }

    @Transactional
    public SanPham capNhatSanPham(Integer id, SanPham updateData) {
        SanPham target = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm TrendFit không tồn tại!"));

        target.setTen(updateData.getTen());
        target.setMoTa(updateData.getMoTa());
        target.setDangBan(updateData.getDangBan());
        target.setGioiTinh(updateData.getGioiTinh());
        target.setChatLieu(updateData.getChatLieu());
        target.setXuatXu(updateData.getXuatXu());
        target.setNamRaMat(updateData.getNamRaMat());
        target.setThanhPhanChatLieu(updateData.getThanhPhanChatLieu());

        if (updateData.getSlug() != null && !updateData.getSlug().trim().isEmpty()) {
            target.setSlug(updateData.getSlug());
        }

        return sanPhamRepository.save(target);
    }

    @Transactional
    public void xoaSanPham(Integer id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }
        sanPhamRepository.deleteById(id);
    }
}