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

    // 1. Cập nhật thông tin cơ bản
    existingSp.setTen(dto.getSanPham().getTen());
    existingSp.setDanhMuc(dto.getSanPham().getDanhMuc());
    existingSp.setThuongHieu(dto.getSanPham().getThuongHieu());
    existingSp.setMoTa(dto.getSanPham().getMoTa());
    existingSp.setGioiTinh(dto.getSanPham().getGioiTinh());
    existingSp.setChatLieu(dto.getSanPham().getChatLieu());
    existingSp.setXuatXu(dto.getSanPham().getXuatXu());
    existingSp.setNamRaMat(dto.getSanPham().getNamRaMat());

    // 2. Xử lý biến thể (Logic: Cập nhật nếu đã có đơn hàng, Xóa/Thêm nếu chưa)
    List<BienTheSanPham> oldVariants = bienTheRepository.findBySanPham_Id(existingSp.getId());
    
    if (dto.getBienTheSanPhams() != null) {
        // Tạo map để dễ tra cứu biến thể cũ
        Map<Integer, BienTheSanPham> oldVariantMap = new HashMap<>();
        for (BienTheSanPham bt : oldVariants) {
            oldVariantMap.put(bt.getId(), bt);
        }

        for (BienTheSanPham newBt : dto.getBienTheSanPhams()) {
            if (newBt.getId() != null && oldVariantMap.containsKey(newBt.getId())) {
                // Đã tồn tại: Cập nhật thông tin
                BienTheSanPham btToUpdate = oldVariantMap.get(newBt.getId());
                btToUpdate.setKichCoSize(newBt.getKichCoSize());
                btToUpdate.setMauSac(newBt.getMauSac());
                btToUpdate.setGia(newBt.getGia());
                btToUpdate.setSoLuongTon(newBt.getSoLuongTon());
                bienTheRepository.save(btToUpdate);
                oldVariantMap.remove(newBt.getId()); // Đánh dấu đã xử lý
            } else {
                // Biến thể mới: Thêm mới
                newBt.setSanPham(existingSp);
                newBt.setId(null);
                bienTheRepository.save(newBt);
            }
        }
        
        // Những biến thể cũ còn sót lại trong map là những cái không có trong danh sách mới -> Cần xóa
        for (BienTheSanPham btToDelete : oldVariantMap.values()) {
            if (chiTietDonHangRepository.existsByBienTheSanPham_Id(btToDelete.getId())) {
                throw new RuntimeException("Không thể xóa biến thể '" + btToDelete.getKichCoSize() + "/" + btToDelete.getMauSac() + "' vì đã có đơn hàng!");
            }
            bienTheRepository.delete(btToDelete);
        }
    }

    // 3. Xử lý ảnh (Ảnh không liên quan đến đơn hàng nên xóa đi lưu lại cho sạch)
    anhRepository.deleteBySanPham_Id(existingSp.getId());
    anhRepository.flush();
    
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