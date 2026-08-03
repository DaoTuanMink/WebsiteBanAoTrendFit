package com.trendfit.api.modules.interaction.dto;

import lombok.Data;

@Data
public class DanhGiaRequestDTO {

    private Integer nguoiDungId;

    private Integer sanPhamId;

    private Integer chiTietDonHangId;

    private Integer saoXepHang;

    private String tieuDe;

    private String noiDung;
}