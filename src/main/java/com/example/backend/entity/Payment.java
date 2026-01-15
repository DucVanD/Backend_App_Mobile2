package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "VNPAY";

    // VNPay specific fields
    @Column(name = "vnp_txn_ref", unique = true)
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;

    @Column(name = "vnp_bank_code", length = 50)
    private String vnpBankCode;

    @Column(name = "vnp_bank_tran_no")
    private String vnpBankTranNo;

    @Column(name = "vnp_card_type", length = 50)
    private String vnpCardType;

    @Column(name = "vnp_pay_date")
    private LocalDateTime vnpPayDate;

    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    @Column(name = "vnp_transaction_status", length = 10)
    private String vnpTransactionStatus;

    // Status tracking
    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "PENDING"; // PENDING, SUCCESS, FAILED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
