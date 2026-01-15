package com.example.backend.entity;

import com.example.backend.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "voucher_code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả voucher

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE hoặc FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue; // Giá trị giảm (Ví dụ: 10% hoặc 50.000đ)

    @Column(name = "max_discount")
    private BigDecimal maxDiscount; // Giảm tối đa bao nhiêu (dùng cho %)

    @Column(name = "min_order_amount", nullable = false)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO; // Đơn tối thiểu để dùng voucher

    @Column(name = "usage_limit")
    private Integer usageLimit; // Giới hạn số lần dùng

    @Builder.Default
    @Column(name = "used_count")
    private Integer usedCount = 0; // Đã dùng bao nhiêu lần

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    private Integer status = 1; // 1: Active, 0: Inactive

    // Tự động lưu ID người tạo
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy;

    // Tự động lưu ID người sửa
    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}