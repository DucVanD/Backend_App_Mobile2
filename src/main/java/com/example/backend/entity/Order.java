package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    // Khách hàng mua (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thông tin người nhận
    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "receiver_email")
    private String receiverEmail;

    @Column(name = "receiver_address", nullable = false)
    private String receiverAddress;

    private String ward;
    private String district;
    private String note;

    // Voucher (Nullable vì có thể không dùng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "voucher_code")
    private String appliedVoucherCode; // Lưu lại code lúc mua để tra cứu lịch sử dễ hơn

    // Tiền nong (Dùng BigDecimal)
    @Column(nullable = false)
    private BigDecimal subtotal; // Tổng tiền hàng chưa giảm

    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount; // Thực trả = sub + ship - discount

    // Các trạng thái (Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    // Quan hệ 1-N với chi tiết đơn hàng (Để lấy list sản phẩm từ đơn hàng)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    // Auditing
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Integer createdBy; // ID người tạo đơn

    @LastModifiedBy
    @Column(name = "updated_by")
    private Integer updatedBy; // ID người sửa (Admin cập nhật trạng thái)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}