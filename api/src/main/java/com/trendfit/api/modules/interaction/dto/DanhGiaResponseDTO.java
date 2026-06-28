package com.trendfit.api.modules.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DanhGiaResponseDTO {

    private Integer id;

    private Integer nguoiDungId;

    private String tenNguoiDung;

    private Integer sanPhamId;

    private Integer saoXepHang;

    private String tieuDe;

    private String noiDung;

    private Boolean daDuyet;

    private Integer luotThich;

    private LocalDateTime ngayTao;
}