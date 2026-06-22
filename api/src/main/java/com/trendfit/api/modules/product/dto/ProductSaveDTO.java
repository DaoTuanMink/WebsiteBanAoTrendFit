// api/src/main/java/com/trendfit/api/modules/product/dto/ProductSaveDTO.java
package com.trendfit.api.modules.product.dto;

import com.trendfit.api.modules.product.entity.AnhSanPham;
import com.trendfit.api.modules.product.entity.BienTheSanPham;
import com.trendfit.api.modules.product.entity.SanPham;
import lombok.Data;
import java.util.List;

@Data
public class ProductSaveDTO {
    private SanPham sanPham;
    private List<BienTheSanPham> bienTheSanPhams;
    private List<AnhSanPham> anhSanPhams;
}