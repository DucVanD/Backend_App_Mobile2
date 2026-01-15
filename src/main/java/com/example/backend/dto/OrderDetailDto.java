package com.example.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDto {

    private Integer id;

    // Quan hệ: chỉ truyền ID
    private Integer orderId;
    private Integer productId;

    // Thông tin sản phẩm đầy đủ
    private ProductDto product;

    // Convenience fields for mobile app
    private String productName;
    private String productImage;

    private BigDecimal priceBuy;
    private Integer quantity;
    private BigDecimal amount;

    private LocalDateTime createdAt;
}
