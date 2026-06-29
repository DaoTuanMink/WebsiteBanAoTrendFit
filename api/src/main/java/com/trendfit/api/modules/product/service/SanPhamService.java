package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.entity.DonHang;
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
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository;
    

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
    SanPham existingSp = sanPhamRepository.findById(dto.getSanPham().getId())
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

    // 1. Kiểm tra xem có biến thể nào đã nằm trong đơn hàng chưa
    List<BienTheSanPham> oldVariants = bienTheRepository.findBySanPham_Id(existingSp.getId());
    for (BienTheSanPham bt : oldVariants) {
        if (chiTietDonHangRepository.existsByBienTheSanPham_Id(bt.getId())) {
            throw new RuntimeException("Sản phẩm này đã có đơn hàng phát sinh, không thể thay đổi biến thể (Size/Màu)!");
        }
    }

    // 2. Nếu đi được tới đây nghĩa là KHÔNG CÓ ĐƠN HÀNG, bạn có quyền xóa cứng
    existingSp.setTen(dto.getSanPham().getTen());
    existingSp.setDanhMuc(dto.getSanPham().getDanhMuc());
    existingSp.setThuongHieu(dto.getSanPham().getThuongHieu());
    
    // Xóa an toàn vì đã chắc chắn không có đơn hàng liên quan
    bienTheRepository.deleteBySanPham_Id(existingSp.getId());
    anhRepository.deleteBySanPham_Id(existingSp.getId());
    
    bienTheRepository.flush();
    anhRepository.flush();

    // 3. Lưu biến thể mới
    if (dto.getBienTheSanPhams() != null) {
        dto.getBienTheSanPhams().forEach(bt -> { 
            bt.setSanPham(existingSp); 
            bt.setId(null); 
        });
        bienTheRepository.saveAll(dto.getBienTheSanPhams());
    }

    // 4. Lưu ảnh mới
    if (dto.getAnhSanPhams() != null) {
        dto.getAnhSanPhams().forEach(anh -> { 
            anh.setSanPham(existingSp); 
            anh.setId(null); 
        });
        anhRepository.saveAll(dto.getAnhSanPhams());
    }
    
    return sanPhamRepository.save(existingSp);
}

// Trong SanPhamService.java
@Transactional
public void delete(Integer id) {
    try {
        // Cố gắng xóa các thành phần con trước
        bienTheRepository.deleteBySanPham_Id(id);
        anhRepository.deleteBySanPham_Id(id);
        sanPhamRepository.deleteById(id);
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        // Bắt lỗi ràng buộc khóa ngoại (Foreign Key)
        throw new RuntimeException("Đã liên kết với đơn hàng, không thể xóa!");
    }
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

// Sửa phương thức findAll hiện tại thành thế này:
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

public List<ProductDetailDTO> getAllPublicProducts() {
    List<SanPham> list = sanPhamRepository.findAll();
    List<ProductDetailDTO> result = new ArrayList<>();
    for (SanPham sp : list) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setSanPham(sp);
        // Tự load ảnh cho từng sản phẩm
        dto.setAnhSanPhams(anhRepository.findBySanPham_Id(sp.getId()));
        result.add(dto);
    }
    return result;
}


}