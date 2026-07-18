package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import com.trendfit.api.modules.product.dto.ProductDetailDTO;
import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.entity.*;
import com.trendfit.api.modules.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SanPhamService {

    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;
    @Autowired private AnhSanPhamRepository anhRepository;
    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private ThuongHieuRepository thuongHieuRepository;
    @Autowired private KichCoRepository kichCoRepository;
    @Autowired private MauSacRepository mauSacRepository;
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository;

    public Map<String, Object> getMetadata() {
        Map<String, Object> data = new HashMap<>();
        data.put("danhMucs", danhMucRepository.findAll());
        data.put("thuongHieus", thuongHieuRepository.findAll());
        data.put("kichCos", kichCoRepository.findAll());      // Thêm
        data.put("mauSacs", mauSacRepository.findAll());      // Thêm
        return data;
    }

    @Transactional
    public SanPham luuSanPhamFull(ProductSaveDTO dto) {
        SanPham sp = sanPhamRepository.save(dto.getSanPham());

        if (dto.getBienTheSanPhams() != null) {
            dto.getBienTheSanPhams().forEach(bt -> {
                bt.setSanPham(sp);
                bt.setId(null);
            });
            bienTheRepository.saveAll(dto.getBienTheSanPhams());
        }

        if (dto.getAnhSanPhams() != null) {
            dto.getAnhSanPhams().forEach(anh -> {
                anh.setSanPham(sp);
                anh.setId(null);
            });
            anhRepository.saveAll(dto.getAnhSanPhams());
        }
        return sp;
    }

    @Transactional
    public SanPham capNhatSanPhamFull(ProductSaveDTO dto) {
        SanPham existingSp = sanPhamRepository.findById(dto.getSanPham().getId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // Cập nhật thông tin cơ bản
        existingSp.setTen(dto.getSanPham().getTen());
        existingSp.setDanhMuc(dto.getSanPham().getDanhMuc());
        existingSp.setThuongHieu(dto.getSanPham().getThuongHieu());
        existingSp.setMoTa(dto.getSanPham().getMoTa());
        existingSp.setGioiTinh(dto.getSanPham().getGioiTinh());
        existingSp.setChatLieu(dto.getSanPham().getChatLieu());
        existingSp.setXuatXu(dto.getSanPham().getXuatXu());
        existingSp.setNamRaMat(dto.getSanPham().getNamRaMat());

        // Xử lý biến thể
        List<BienTheSanPham> oldVariants = bienTheRepository.findBySanPham_Id(existingSp.getId());
        Map<Integer, BienTheSanPham> oldVariantMap = new HashMap<>();
        oldVariants.forEach(bt -> oldVariantMap.put(bt.getId(), bt));

        if (dto.getBienTheSanPhams() != null) {
            for (BienTheSanPham newBt : dto.getBienTheSanPhams()) {
                if (newBt.getId() != null && oldVariantMap.containsKey(newBt.getId())) {
                    // Cập nhật biến thể cũ
                    BienTheSanPham btToUpdate = oldVariantMap.get(newBt.getId());
                    btToUpdate.setKichCo(newBt.getKichCo());
                    btToUpdate.setMauSac(newBt.getMauSac());
                    btToUpdate.setGia(newBt.getGia());
                    btToUpdate.setGiaSale(newBt.getGiaSale());
                    btToUpdate.setSoLuongTon(newBt.getSoLuongTon());
                    btToUpdate.setDangBan(newBt.getDangBan());
                    bienTheRepository.save(btToUpdate);
                    oldVariantMap.remove(newBt.getId());
                } else {
                    // Thêm biến thể mới
                    newBt.setSanPham(existingSp);
                    newBt.setId(null);
                    bienTheRepository.save(newBt);
                }
            }

            // Xóa biến thể không còn sử dụng
            for (BienTheSanPham btToDelete : oldVariantMap.values()) {
                if (chiTietDonHangRepository.existsByBienTheSanPham_Id(btToDelete.getId())) {
                    throw new RuntimeException("Không thể xóa biến thể vì đã có đơn hàng liên kết!");
                }
                bienTheRepository.delete(btToDelete);
            }
        }

        // Xử lý ảnh
        anhRepository.deleteBySanPham_Id(existingSp.getId());
        if (dto.getAnhSanPhams() != null) {
            dto.getAnhSanPhams().forEach(anh -> {
                anh.setSanPham(existingSp);
                anh.setId(null);
            });
            anhRepository.saveAll(dto.getAnhSanPhams());
        }

        return sanPhamRepository.save(existingSp);
    }

    @Transactional
    public void delete(Integer id) {
        try {
            bienTheRepository.deleteBySanPham_Id(id);
            anhRepository.deleteBySanPham_Id(id);
            sanPhamRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Không thể xóa sản phẩm vì đã có đơn hàng liên kết!");
        }
    }

    public List<SanPham> findAll() {
        return sanPhamRepository.findAll();
    }

    public List<ProductDetailDTO> findAllFull() {
        List<SanPham> listSp = sanPhamRepository.findAll();
        List<ProductDetailDTO> listDto = new ArrayList<>();

        for (SanPham sp : listSp) {
            ProductDetailDTO dto = new ProductDetailDTO();
            dto.setSanPham(sp);
            dto.setBienTheSanPhams(bienTheRepository.findBySanPham_Id(sp.getId()));
            dto.setAnhSanPhams(anhRepository.findBySanPham_Id(sp.getId()));
            listDto.add(dto);
        }
        return listDto;
    }

    // Trong SanPhamService.java
public List<SanPham> getAllPublicProducts() {
    List<SanPham> list = sanPhamRepository.findAll();
    return list != null ? list : new ArrayList<>(); // Đảm bảo luôn trả về List rỗng nếu không có dữ liệu
}

    public ProductDetailDTO findByIdFull(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setSanPham(sp);
        dto.setBienTheSanPhams(bienTheRepository.findBySanPham_Id(id));
        dto.setAnhSanPhams(anhRepository.findBySanPham_Id(id));
        return dto;
    }

    public List<BienTheSanPham> findBySanPhamId(Integer id) {
    return bienTheRepository.findBySanPham_Id(id);
}
}