package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import com.trendfit.api.modules.product.dto.ProductDetailDTO;
import com.trendfit.api.modules.product.dto.ProductSaveDTO;
import com.trendfit.api.modules.product.dto.RelatedProductDTO;
import com.trendfit.api.modules.product.entity.*;
import com.trendfit.api.modules.product.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        existingSp.setDangBan(dto.getSanPham().getDangBan());

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

public List<ProductDetailDTO> getAllPublicProducts() {
    // Chỉ lấy những sản phẩm có dangBan bằng true (đang kinh doanh)
    List<SanPham> listSp = sanPhamRepository.findAll().stream()
            .filter(sp -> Boolean.TRUE.equals(sp.getDangBan()))
            .toList();
            
    List<ProductDetailDTO> listDto = new ArrayList<>();

    for (SanPham sp : listSp) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setSanPham(sp);
        
        // (Tùy chọn) Bạn cũng có thể lọc luôn các biến thể nào ngừng bán nếu muốn
        List<BienTheSanPham> activeVariants = bienTheRepository.findBySanPham_Id(sp.getId()).stream()
                .filter(bt -> Boolean.TRUE.equals(bt.getDangBan()))
                .toList();
                
        dto.setBienTheSanPhams(activeVariants);
        dto.setAnhSanPhams(anhRepository.findBySanPham_Id(sp.getId()));
        listDto.add(dto);
    }
    return listDto;
}

    public ProductDetailDTO findByIdFull(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setSanPham(sp);
        dto.setBienTheSanPhams(bienTheRepository.findBySanPham_Id(id));
        dto.setAnhSanPhams(anhRepository.findBySanPham_Id(id));
        dto.setSanPhamLienQuan(timSanPhamLienQuan(sp));
        return dto;
    }

    /**
     * Xây danh sách "Sản phẩm liên quan" cho trang chi tiết sản phẩm.
     * Dựa trên DanhMuc hoặc ThuongHieu của sản phẩm đang xem (yêu cầu của Đạt).
     */
    private List<RelatedProductDTO> timSanPhamLienQuan(SanPham sp) {
        Integer danhMucId = sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null;
        Integer thuongHieuId = sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null;

        // Không có danh mục lẫn thương hiệu thì không có cơ sở để gợi ý
        if (danhMucId == null && thuongHieuId == null) {
            return new ArrayList<>();
        }

        Pageable top8 = PageRequest.of(0, 8);
        List<SanPham> lienQuan = sanPhamRepository.timSanPhamLienQuan(sp.getId(), danhMucId, thuongHieuId, top8);

        List<RelatedProductDTO> result = new ArrayList<>();
        for (SanPham item : lienQuan) {
            List<AnhSanPham> anhList = anhRepository.findBySanPham_Id(item.getId());
            String anhChinh = anhList.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getLaAnhChinh()))
                    .map(AnhSanPham::getUrlAnh)
                    .findFirst()
                    .orElse(anhList.isEmpty() ? null : anhList.get(0).getUrlAnh());

            List<BienTheSanPham> bienTheList = bienTheRepository.findBySanPham_Id(item.getId());
            BigDecimal giaTu = bienTheList.stream()
                    .map(bt -> bt.getGiaSale() != null ? bt.getGiaSale() : bt.getGia())
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            result.add(new RelatedProductDTO(
                    item.getId(),
                    item.getTen(),
                    item.getDanhGiaTrungBinh(),
                    item.getTongLuotDanhGia(),
                    anhChinh,
                    giaTu
            ));
        }
        return result;
    }

    public List<BienTheSanPham> findBySanPhamId(Integer id) {
    return bienTheRepository.findBySanPham_Id(id);
}
}