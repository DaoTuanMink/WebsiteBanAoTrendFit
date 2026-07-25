package com.trendfit.api.modules.product.dto;

import com.trendfit.api.modules.product.entity.*;
import lombok.Data;
import java.util.List;

@Data
public class ProductDetailDTO {
    private SanPham sanPham;
    private List<BienTheSanPham> bienTheSanPhams;
    private List<AnhSanPham> anhSanPhams;
    // Danh sách sản phẩm gợi ý (cùng Danh Mục hoặc cùng Thương Hiệu)
    private List<RelatedProductDTO> sanPhamLienQuan;
}