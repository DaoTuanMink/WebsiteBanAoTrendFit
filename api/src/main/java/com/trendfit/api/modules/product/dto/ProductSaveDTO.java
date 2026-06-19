package com.trendfit.api.modules.product.dto;

import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.entity.AnhSanPham;
import lombok.Data;
import java.util.List;

@Data
public class ProductSaveDTO {
    private SanPham sanPham;
    private List<BienTheSanPham> bienThes;
    private List<AnhSanPham> anhs;
}