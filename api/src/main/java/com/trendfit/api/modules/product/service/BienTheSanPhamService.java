package com.trendfit.api.modules.product.service;

import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.repository.BienTheSanPhamRepository;
import com.trendfit.api.modules.product.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BienTheSanPhamService {

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Lấy biến thể theo ID sản phẩm (Dùng Integer cho đồng bộ với Entity)
    public List<BienTheSanPham> getBySanPhamId(Integer sanPhamId) {
        return bienTheSanPhamRepository.findBySanPham_Id(sanPhamId);
    }

    // Lưu danh sách biến thể
    @Transactional
    public List<BienTheSanPham> saveAll(Integer sanPhamId, List<BienTheSanPham> variants) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + sanPhamId));

        // Trước khi lưu mới, nên xóa các biến thể cũ (nếu yêu cầu cập nhật lại toàn bộ)
        // Nếu bạn muốn lưu đè, hãy dùng hàm này:
        bienTheSanPhamRepository.deleteBySanPham_Id(sanPhamId);

        // Gán sản phẩm cha cho từng biến thể
        for (BienTheSanPham v : variants) {
            v.setSanPham(sanPham);
            v.setId(null); // Đảm bảo Hibernate hiểu đây là bản ghi mới
        }

        return bienTheSanPhamRepository.saveAll(variants);
    }
}