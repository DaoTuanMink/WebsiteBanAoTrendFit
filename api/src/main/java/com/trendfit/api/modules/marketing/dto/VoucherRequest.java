package com.trendfit.api.modules.marketing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherRequest {
    private String ma;
    private BigDecimal tongDon;
}