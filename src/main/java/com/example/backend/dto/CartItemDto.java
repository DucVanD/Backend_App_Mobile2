package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    private Integer id;
    private Integer cartId;
    private Integer productId;

    // Product info for display
    private ProductDto product;

    private Integer quantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
