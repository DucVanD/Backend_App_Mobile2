package com.example.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CategoryDto {

    private Integer id;
    @NotBlank(message = "Tên danh mục không được trống")
    @Size(min = 3, max = 100, message = "Tên danh mục phải từ 3-100 ký tự")
    private String name;

    // Slug sẽ tự động sinh từ name, không cần @NotBlank
    private String slug;
    private String image;
    private String imagePublicId;
    private String description;
    private Integer status;
    private Integer parentId;
    private Long productCount; // Số lượng sản phẩm
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private java.util.List<CategoryDto> children;
}
