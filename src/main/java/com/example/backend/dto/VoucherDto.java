package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.backend.entity.enums.DiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDto {

    private Integer id;

    @NotBlank(message = "Mã voucher không được để trống")
    private String voucherCode;

    @NotBlank(message = "Tên voucher không được để trống")
    private String name;

    private String description; // Mô tả voucher

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @jakarta.validation.constraints.Min(value = 0, message = "Giá trị giảm giá không được âm")
    private BigDecimal discountValue;

    @jakarta.validation.constraints.Min(value = 0, message = "Giá trị giảm tối đa không được âm")
    private BigDecimal maxDiscount;

    @jakarta.validation.constraints.Min(value = 0, message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderAmount;

    @jakarta.validation.constraints.Min(value = 1, message = "Giới hạn sử dụng phải ít nhất là 1")
    private Integer usageLimit;
    private Integer usedCount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    private Integer status;

    // Auditing
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
