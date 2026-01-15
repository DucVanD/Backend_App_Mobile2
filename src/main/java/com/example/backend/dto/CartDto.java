package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

    private Integer id;
    private Integer userId;

    private List<CartItemDto> items;

    private Integer totalItems;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
