package com.trendfit.api.modules.product.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProductSaveDTO {
    private Integer id;
    private String ten;
    private String moTa;
    private String chatLieu;
    private Boolean dangBan;
    private Integer danhMucId;
    private Integer thuongHieuId;
    
    // Danh sách biến thể (Size, Màu, Giá, Tồn kho)
    private List<BienTheDTO> bienThes;

    @Data
    public static class BienTheDTO {
        private String size;
        private String mauSac;
        private Integer soLuongTon;
        private Double gia;
    }
}