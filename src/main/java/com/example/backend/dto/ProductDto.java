package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Integer id;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
    private String categoryName;

    @NotBlank(message = "Tên sản phẩm không được trống")
    @Size(min = 3, max = 200, message = "Tên sản phẩm phải từ 3-200 ký tự")
    private String name;

    private String slug;
    private String image;
    private String imagePublicId;
    private String description;
    private String detail; // Detail có thể null

    @Min(value = 0, message = "Số lượng không được âm")
    private Integer qty;

    @NotNull(message = "Giá bán không được trống")
    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal salePrice;

    @Min(value = 0, message = "Giá giảm không được âm")
    private BigDecimal discountPrice;

    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
