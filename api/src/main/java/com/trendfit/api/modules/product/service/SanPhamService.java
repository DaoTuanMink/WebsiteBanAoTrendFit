package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.SanPhamRepository;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    public List<SanPham> timKiemSanPhamAdmin(String search, Long danhMucId, Long thuongHieuId) {
        // Tận dụng Spring Data JPA custom query hoặc native query tương ứng trong Repository của bạn
        if (danhMucId != null && thuongHieuId != null) {
            return sanPhamRepository.findByTenContainingAndDanhMucIdAndThuongHieuId(search, danhMucId, thuongHieuId);
        } else if (danhMucId != null) {
            return sanPhamRepository.findByTenContainingAndDanhMucId(search, danhMucId);
        } else if (thuongHieuId != null) {
            return sanPhamRepository.findByTenContainingAndThuongHieuId(search, thuongHieuId);
        }
        return sanPhamRepository.findByTenContaining(search);
    }

    @Transactional
    public SanPham themMoiSanPhm(SanPham sanPham) {
        if (sanPham.getSlug() == null || sanPham.getSlug().trim().isEmpty()) {
            sanPham.setSlug("trendfit-" + UUID.randomUUID().toString().substring(0, 8));
        }
        return sanPhamRepository.save(sanPham);
    }

    @Transactional
    public SanPham capNhatSanPham(Long id, SanPham updateData) {
        SanPham target = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        target.setTen(updateData.getTen());
        target.setMoTa(updateData.getMoTa());
        target.setGiaCoBan(updateData.getGiaCoBan());
        target.setAnhChinh(updateData.getAnhChinh());
        target.setTrangThaiKinhDoanh(updateData.getTrangThaiKinhDoanh());

        if (updateData.getSlug() != null && !updateData.getSlug().trim().isEmpty()) {
            target.setSlug(updateData.getSlug());
        }

        return sanPhamRepository.save(target);
    }

    @Transactional
    public void xoaSanPham(Long id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }
        sanPhamRepository.deleteById(id);
    }
}