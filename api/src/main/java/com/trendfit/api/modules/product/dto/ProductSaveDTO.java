package com.trendfit.api.modules.product.dto;

import com.trendfit.api.modules.product.entity.*;
import lombok.Data;
import java.util.List;

@Data
public class ProductSaveDTO {
    private SanPham sanPham;
    private List<BienTheSanPham> bienTheSanPhams;
    private List<AnhSanPham> anhSanPhams;
}