package com.example.backend.dto;

import com.example.backend.entity.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherCheckResponse {
    @JsonProperty("isValid")
    private boolean isValid;
    private BigDecimal discountAmount;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private String voucherCode;
    private String message;
}
