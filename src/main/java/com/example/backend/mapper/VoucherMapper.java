package com.example.backend.mapper;

import com.example.backend.dto.VoucherDto;
import com.example.backend.entity.Voucher;

public class VoucherMapper {

    // Entity → DTO
    public static VoucherDto toDto(Voucher voucher) {
        if (voucher == null)
            return null;

        return VoucherDto.builder()
                .id(voucher.getId())
                .voucherCode(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscount(voucher.getMaxDiscount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus())
                .createdBy(voucher.getCreatedBy())
                .updatedBy(voucher.getUpdatedBy())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }

    // DTO → Entity (CREATE)
    public static Voucher toEntity(VoucherDto dto) {
        if (dto == null)
            return null;

        Voucher.VoucherBuilder builder = Voucher.builder()
                .code(dto.getVoucherCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .maxDiscount(dto.getMaxDiscount())
                .usageLimit(dto.getUsageLimit())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate());

        // Only set these if they are not null to respect entity-level @Builder.Default
        if (dto.getMinOrderAmount() != null) {
            builder.minOrderAmount(dto.getMinOrderAmount());
        }
        if (dto.getUsedCount() != null) {
            builder.usedCount(dto.getUsedCount());
        }
        if (dto.getStatus() != null) {
            builder.status(dto.getStatus());
        }

        return builder.build();
    }
}
