package com.example.backend.repository;

import com.example.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByPaymentStatus(String paymentStatus);
}
