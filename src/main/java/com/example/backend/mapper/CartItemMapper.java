package com.example.backend.mapper;

import com.example.backend.dto.CartItemDto;
import com.example.backend.entity.CartItem;

public class CartItemMapper {

    public static CartItemDto toDto(CartItem entity) {
        if (entity == null)
            return null;

        return CartItemDto.builder()
                .id(entity.getId())
                .cartId(entity.getCart() != null ? entity.getCart().getId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .product(entity.getProduct() != null
                        ? ProductMapper.toDto(entity.getProduct())
                        : null)
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
