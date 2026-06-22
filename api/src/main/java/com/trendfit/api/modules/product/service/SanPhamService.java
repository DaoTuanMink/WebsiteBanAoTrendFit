package com.trendfit.api.modules.product.service;

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

    public Map<String, Object> getMetadata() {
        Map<String, Object> data = new HashMap<>();
        data.put("danhMucs", danhMucRepository.findAll());
        data.put("thuongHieus", thuongHieuRepository.findAll());
        return data;
    }

    @Transactional
    public SanPham luuSanPhamFull(ProductSaveDTO dto) {
        SanPham sp = sanPhamRepository.save(dto.getSanPham());
        if (dto.getBienTheSanPhams() != null) {
            dto.getBienTheSanPhams().forEach(bt -> { bt.setSanPham(sp); bt.setId(null); });
            bienTheRepository.saveAll(dto.getBienTheSanPhams());
        }
        if (dto.getAnhSanPhams() != null) {
            dto.getAnhSanPhams().forEach(anh -> { anh.setSanPham(sp); anh.setId(null); });
            anhRepository.saveAll(dto.getAnhSanPhams());
        }
        return sp;
    }

    public List<SanPham> findAll() {
    return sanPhamRepository.findAll();
}

@Transactional
public SanPham capNhatSanPhamFull(ProductSaveDTO dto) {
    SanPham sp = sanPhamRepository.save(dto.getSanPham());
    
    // Xóa biến thể/ảnh cũ để cập nhật mới (cách nhanh nhất)
    bienTheRepository.deleteBySanPham_Id(sp.getId());
    anhRepository.deleteBySanPham_Id(sp.getId());

    // Thêm lại biến thể/ảnh mới
    if (dto.getBienTheSanPhams() != null) {
        dto.getBienTheSanPhams().forEach(bt -> { bt.setSanPham(sp); bt.setId(null); });
        bienTheRepository.saveAll(dto.getBienTheSanPhams());
    }
    // ... làm tương tự cho ảnh
    return sp;
}

public void delete(Integer id) {
    // Lưu ý: Phải xóa biến thể và ảnh trước khi xóa sản phẩm (do ràng buộc khóa ngoại)
    bienTheRepository.deleteBySanPham_Id(id);
    anhRepository.deleteBySanPham_Id(id);
    sanPhamRepository.deleteById(id);
}

public List<BienTheSanPham> findBySanPhamId(Integer id) {
    return bienTheRepository.findBySanPham_Id(id);
}
}