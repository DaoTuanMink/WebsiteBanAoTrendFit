package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.entity.*;
import com.trendfit.api.modules.product.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final AnhSanPhamRepository anhSanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    public SanPhamService(
            SanPhamRepository sanPhamRepository,
            BienTheSanPhamRepository bienTheSanPhamRepository,
            AnhSanPhamRepository anhSanPhamRepository,
            DanhMucRepository danhMucRepository,
            ThuongHieuRepository thuongHieuRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.bienTheSanPhamRepository = bienTheSanPhamRepository;
        this.anhSanPhamRepository = anhSanPhamRepository;
        this.danhMucRepository = danhMucRepository;
        this.thuongHieuRepository = thuongHieuRepository;
    }

    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    public List<SanPham> searchByTen(String keyword) {
        return sanPhamRepository.findByTenContainingIgnoreCase(keyword.trim());
    }

    public ProductSaveDTO getFullProduct(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm với ID: " + id));

        ProductSaveDTO dto = new ProductSaveDTO();
        dto.setSanPham(sp);
        dto.setBienThes(bienTheSanPhamRepository.findBySanPham_Id(id));
        dto.setAnhs(anhSanPhamRepository.findBySanPham_IdOrderByThuTuAsc(id));
        return dto;
    }

    @Transactional
    public SanPham saveFullProduct(ProductSaveDTO dto) {
        SanPham sp = prepareSanPham(dto.getSanPham());
        SanPham spSaved = sanPhamRepository.save(sp);
        saveVariantsAndImages(spSaved, dto);
        return spSaved;
    }

    @Transactional
    public SanPham updateFullProduct(Integer id, ProductSaveDTO dto) {
        SanPham existing = sanPhamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm với ID: " + id));

        SanPham incoming = dto.getSanPham();
        existing.setTen(incoming.getTen());
        existing.setSlug(incoming.getSlug());
        existing.setGioiTinh(incoming.getGioiTinh());
        existing.setChatLieu(incoming.getChatLieu());
        existing.setXuatXu(incoming.getXuatXu());
        existing.setNamRaMat(incoming.getNamRaMat());
        existing.setMoTa(incoming.getMoTa());
        existing.setThanhPhanChatLieu(incoming.getThanhPhanChatLieu());
        existing.setDangBan(incoming.getDangBan() != null ? incoming.getDangBan() : true);
        applyRelations(existing, incoming);

        SanPham updated = sanPhamRepository.save(existing);

        bienTheSanPhamRepository.deleteBySanPham_Id(id);
        anhSanPhamRepository.deleteBySanPham_Id(id);
        saveVariantsAndImages(updated, dto);

        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy sản phẩm với ID: " + id);
        }
        bienTheSanPhamRepository.deleteBySanPham_Id(id);
        anhSanPhamRepository.deleteBySanPham_Id(id);
        sanPhamRepository.deleteById(id);
    }

    public List<SanPham> layTatCaSanPhamChoTrangChu() {
        return sanPhamRepository.findAll();
    }

    public SanPham layChiTietSanPham(Integer id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm với ID: " + id));
    }

    public List<BienTheSanPham> layBienTheCuaSanPham(Integer sanPhamId) {
        return bienTheSanPhamRepository.findBySanPham_Id(sanPhamId);
    }

    private SanPham prepareSanPham(SanPham sp) {
        SanPham prepared = new SanPham();
        prepared.setTen(sp.getTen());
        prepared.setSlug(sp.getSlug());
        prepared.setGioiTinh(sp.getGioiTinh());
        prepared.setChatLieu(sp.getChatLieu());
        prepared.setXuatXu(sp.getXuatXu());
        prepared.setNamRaMat(sp.getNamRaMat());
        prepared.setMoTa(sp.getMoTa());
        prepared.setThanhPhanChatLieu(sp.getThanhPhanChatLieu());
        prepared.setDangBan(sp.getDangBan() != null ? sp.getDangBan() : true);
        applyRelations(prepared, sp);
        return prepared;
    }

    private void applyRelations(SanPham target, SanPham source) {
        if (source.getDanhMuc() != null && source.getDanhMuc().getId() != null) {
            target.setDanhMuc(danhMucRepository.getReferenceById(source.getDanhMuc().getId()));
        }
        if (source.getThuongHieu() != null && source.getThuongHieu().getId() != null) {
            target.setThuongHieu(thuongHieuRepository.getReferenceById(source.getThuongHieu().getId()));
        }
    }

    private void saveVariantsAndImages(SanPham spSaved, ProductSaveDTO dto) {
        if (dto.getBienThes() != null && !dto.getBienThes().isEmpty()) {
            for (BienTheSanPham bt : dto.getBienThes()) {
                bt.setId(null);
                bt.setSanPham(spSaved);
            }
            bienTheSanPhamRepository.saveAll(dto.getBienThes());
        }

        if (dto.getAnhs() != null && !dto.getAnhs().isEmpty()) {
            for (AnhSanPham anh : dto.getAnhs()) {
                anh.setId(null);
                anh.setSanPham(spSaved);
            }
            anhSanPhamRepository.saveAll(dto.getAnhs());
        }
    }
}
