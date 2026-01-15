package com.example.backend.mapper;

import com.example.backend.dto.CartDto;
import com.example.backend.entity.Cart;

import java.util.stream.Collectors;

public class CartMapper {

    public static CartDto toDto(Cart entity) {
        if (entity == null)
            return null;

        return CartDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .items(entity.getCartItems() != null
                        ? entity.getCartItems().stream()
                                .map(CartItemMapper::toDto)
                                .collect(Collectors.toList())
                        : null)
                .totalItems(entity.getTotalItems())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
