package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.entity.DonHang;
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
    // 1. Kiểm tra xem sản phẩm có tồn tại không
    SanPham existingSp = sanPhamRepository.findById(dto.getSanPham().getId())
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

    // 2. Cập nhật thông tin cơ bản
    existingSp.setTen(dto.getSanPham().getTen());
    existingSp.setDanhMuc(dto.getSanPham().getDanhMuc());
    existingSp.setThuongHieu(dto.getSanPham().getThuongHieu());
    
    // 3. Xóa dữ liệu cũ (Đã có logic delete trong Repository)
    bienTheRepository.deleteBySanPham_Id(existingSp.getId());
    anhRepository.deleteBySanPham_Id(existingSp.getId());
    
    // Cần flush để DB thực thi lệnh xóa trước khi thêm mới
    bienTheRepository.flush();
    anhRepository.flush();

    // 4. Lưu biến thể mới
    if (dto.getBienTheSanPhams() != null) {
        dto.getBienTheSanPhams().forEach(bt -> { 
            bt.setSanPham(existingSp); 
            bt.setId(null); // Luôn set null ID để JPA tự tạo mới
        });
        bienTheRepository.saveAll(dto.getBienTheSanPhams());
    }

    // 5. Lưu ảnh mới
    if (dto.getAnhSanPhams() != null) {
        dto.getAnhSanPhams().forEach(anh -> { 
            anh.setSanPham(existingSp); 
            anh.setId(null); 
        });
        anhRepository.saveAll(dto.getAnhSanPhams());
    }
    
    return sanPhamRepository.save(existingSp);
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

public ProductDetailDTO findByIdFull(Integer id) {
    SanPham sp = sanPhamRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

    ProductDetailDTO dto = new ProductDetailDTO();
    dto.setSanPham(sp);
    dto.setBienTheSanPhams(bienTheRepository.findBySanPham_Id(id));
    dto.setAnhSanPhams(anhRepository.findBySanPham_Id(id));
    
    return dto;
}

}