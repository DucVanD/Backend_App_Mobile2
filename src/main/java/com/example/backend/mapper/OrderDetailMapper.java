package com.example.backend.mapper;

import com.example.backend.dto.OrderDetailDto;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderDetail;
import com.example.backend.entity.Product;

public class OrderDetailMapper {

    // Entity → DTO
    public static OrderDetailDto toDto(OrderDetail entity) {
        if (entity == null)
            return null;

        return OrderDetailDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .product(entity.getProduct() != null
                        ? com.example.backend.mapper.ProductMapper.toDto(entity.getProduct())
                        : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .productImage(entity.getProduct() != null ? entity.getProduct().getImage() : null)
                .priceBuy(entity.getPriceBuy())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // DTO → Entity (CREATE) - Simplified version without Product parameter
    public static OrderDetail toEntity(
            OrderDetailDto dto,
            Order order) {
        if (dto == null)
            return null;

        // Create a Product reference with just the ID
        Product product = new Product();
        product.setId(dto.getProductId());

        // Calculate amount if not provided
        java.math.BigDecimal amount = dto.getAmount();
        if (amount == null && dto.getPriceBuy() != null && dto.getQuantity() != null) {
            amount = dto.getPriceBuy().multiply(java.math.BigDecimal.valueOf(dto.getQuantity()));
        }

        return OrderDetail.builder()
                .order(order)
                .product(product)
                .priceBuy(dto.getPriceBuy())
                .quantity(dto.getQuantity())
                .amount(amount)
                .build();
    }

    // DTO → Entity (CREATE) - Full version with Product lookup
    public static OrderDetail toEntity(
            OrderDetailDto dto,
            Order order,
            Product product) {
        if (dto == null)
            return null;

        // Calculate amount if not provided
        java.math.BigDecimal amount = dto.getAmount();
        if (amount == null && dto.getPriceBuy() != null && dto.getQuantity() != null) {
            amount = dto.getPriceBuy().multiply(java.math.BigDecimal.valueOf(dto.getQuantity()));
        }

        return OrderDetail.builder()
                .order(order)
                .product(product)
                .priceBuy(dto.getPriceBuy())
                .quantity(dto.getQuantity())
                .amount(amount)
                .build();
    }
}
