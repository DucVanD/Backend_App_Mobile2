package com.example.backend.mapper;

import com.example.backend.dto.OrderDto;
import com.example.backend.entity.*;

import java.util.stream.Collectors;

public class OrderMapper {

        // Entity → DTO
        public static OrderDto toDto(Order order) {
                if (order == null)
                        return null;

                return OrderDto.builder()
                                .id(order.getId())
                                .orderCode(order.getOrderCode())
                                .userId(order.getUser() != null ? order.getUser().getId() : null)
                                .receiverName(order.getReceiverName())
                                .receiverPhone(order.getReceiverPhone())
                                .receiverEmail(order.getReceiverEmail())
                                .receiverAddress(order.getReceiverAddress())
                                .ward(order.getWard())
                                .district(order.getDistrict())
                                .note(order.getNote())
                                .voucherId(order.getVoucher() != null ? order.getVoucher().getId() : null)
                                .voucherCode(order.getAppliedVoucherCode())
                                .subtotal(order.getSubtotal())
                                .shippingFee(order.getShippingFee())
                                .discountAmount(order.getDiscountAmount())
                                .totalAmount(order.getTotalAmount())
                                .paymentMethod(order.getPaymentMethod())
                                .paymentStatus(order.getPaymentStatus())
                                .status(order.getStatus())
                                .cancelReason(order.getCancelReason())
                                .orderDetails(
                                                order.getOrderDetails() != null
                                                                ? order.getOrderDetails().stream()
                                                                                .map(OrderDetailMapper::toDto)
                                                                                .collect(Collectors.toList())
                                                                : null)
                                .createdBy(order.getCreatedBy())
                                .updatedBy(order.getUpdatedBy())
                                .createdAt(order.getCreatedAt())
                                .updatedAt(order.getUpdatedAt())
                                .completedAt(order.getCompletedAt())
                                .build();
        }

        // DTO → Entity (CREATE)
        public static Order toEntity(
                        OrderDto dto,
                        User user,
                        Voucher voucher) {
                if (dto == null)
                        return null;

                return Order.builder()
                                .orderCode(dto.getOrderCode())
                                .user(user)
                                .receiverName(dto.getReceiverName())
                                .receiverPhone(dto.getReceiverPhone())
                                .receiverEmail(dto.getReceiverEmail())
                                .receiverAddress(dto.getReceiverAddress())
                                .ward(dto.getWard())
                                .district(dto.getDistrict())
                                .note(dto.getNote())
                                .voucher(voucher)
                                .appliedVoucherCode(dto.getVoucherCode())
                                .subtotal(dto.getSubtotal())
                                .shippingFee(dto.getShippingFee())
                                .discountAmount(dto.getDiscountAmount())
                                .totalAmount(dto.getTotalAmount())
                                .paymentMethod(dto.getPaymentMethod())
                                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus()
                                                : com.example.backend.entity.enums.PaymentStatus.UNPAID)
                                .status(dto.getStatus() != null ? dto.getStatus()
                                                : com.example.backend.entity.enums.OrderStatus.PENDING)
                                .cancelReason(dto.getCancelReason())
                                .completedAt(dto.getCompletedAt())
                                .build();
        }
}
