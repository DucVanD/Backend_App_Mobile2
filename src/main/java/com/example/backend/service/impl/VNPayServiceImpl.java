package com.example.backend.service.impl;

import com.example.backend.config.VNPayConfig;
import com.example.backend.entity.Order;
import com.example.backend.entity.Payment;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.VNPayService;
import com.example.backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public String createPaymentUrl(Long orderId, HttpServletRequest request) throws Exception {
        Order order = orderRepository.findById(orderId.intValue())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Tạo vnp_TxnRef unique
        String vnpTxnRef = "ORDER" + orderId + "_" + System.currentTimeMillis();

        // Tạo Payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod("VNPAY");
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setPaymentStatus("PENDING");
        paymentRepository.save(payment);

        // Tạo các tham số VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVnpVersion());
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());

        // Số tiền nhân với 100 để triệt tiêu phần thập phân
        long amount = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnpParams.put("vnp_OrderType", "billpayment");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        // Thời gian tạo và hết hạn
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Tạo secure hash
        String queryUrl = VNPayUtil.hashAllFields(vnpParams);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), queryUrl);

        // Debug logging
        String paymentUrl = vnPayConfig.getVnpApiUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
        System.out.println("=== VNPay Payment URL Debug ===");
        System.out.println("TmnCode: " + vnPayConfig.getVnpTmnCode());
        System.out.println("HashSecret: " + vnPayConfig.getVnpHashSecret());
        System.out.println("Query String: " + queryUrl);
        System.out.println("SecureHash: " + vnpSecureHash);
        System.out.println("Full URL: " + paymentUrl);
        System.out.println("===============================");

        // Tạo URL cuối cùng
        return paymentUrl;
    }

    @Override
    @Transactional
    public Map<String, String> handleCallback(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            // Verify signature
            String signValue = VNPayUtil.hashAllFields(params);
            String checkSum = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), signValue);

            if (checkSum.equals(vnpSecureHash)) {
                String vnpTxnRef = params.get("vnp_TxnRef");
                String vnpResponseCode = params.get("vnp_ResponseCode");
                String vnpTransactionStatus = params.get("vnp_TransactionStatus");

                // Update payment
                Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
                        .orElseThrow(() -> new RuntimeException("Payment not found with txnRef: " + vnpTxnRef));

                payment.setVnpResponseCode(vnpResponseCode);
                payment.setVnpTransactionStatus(vnpTransactionStatus);

                // Safely set optional fields (may be null when payment is cancelled)
                if (params.get("vnp_TransactionNo") != null) {
                    payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
                }
                if (params.get("vnp_BankCode") != null) {
                    payment.setVnpBankCode(params.get("vnp_BankCode"));
                }
                if (params.get("vnp_BankTranNo") != null) {
                    payment.setVnpBankTranNo(params.get("vnp_BankTranNo"));
                }
                if (params.get("vnp_CardType") != null) {
                    payment.setVnpCardType(params.get("vnp_CardType"));
                }

                // Parse vnp_PayDate
                String vnpPayDate = params.get("vnp_PayDate");
                if (vnpPayDate != null && !vnpPayDate.isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        payment.setVnpPayDate(LocalDateTime.parse(vnpPayDate, formatter));
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to parse vnp_PayDate: " + e.getMessage());
                    }
                }

                Order order = payment.getOrder();

                // Update status
                if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                    // Payment successful

                    // ⚠️ CRITICAL: Check if order is still in valid state for payment
                    if (order.getStatus() != com.example.backend.entity.enums.OrderStatus.PENDING) {
                        // Order has been cancelled or is in invalid state
                        payment.setPaymentStatus("REJECTED");
                        paymentRepository.save(payment);

                        result.put("status", "error");
                        result.put("message", "Đơn hàng không hợp lệ hoặc đã bị hủy");
                        result.put("orderId", String.valueOf(order.getId()));

                        System.err.println("❌ Payment rejected - Order status is not PENDING: " + order.getStatus());
                        return result;
                    }

                    payment.setPaymentStatus("SUCCESS");
                    order.setStatus(com.example.backend.entity.enums.OrderStatus.PENDING);
                    order.setPaymentStatus(com.example.backend.entity.enums.PaymentStatus.PAID);
                    orderRepository.save(order);

                    result.put("status", "success");
                    result.put("message", "Thanh toán thành công");
                    result.put("orderId", String.valueOf(order.getId()));
                } else {
                    // Payment failed or cancelled
                    payment.setPaymentStatus("CANCELLED");

                    // Cancel order and restore stock
                    order.setStatus(com.example.backend.entity.enums.OrderStatus.CANCELLED);
                    order.setPaymentStatus(com.example.backend.entity.enums.PaymentStatus.UNPAID);
                    order.setCancelReason("Hủy thanh toán VNPay - Mã lỗi: " + vnpResponseCode);

                    // Restore stock for all products in order
                    for (com.example.backend.entity.OrderDetail detail : order.getOrderDetails()) {
                        if (detail.getProduct() != null) {
                            com.example.backend.entity.Product product = detail.getProduct();
                            int currentQty = product.getQty() != null ? product.getQty() : 0;
                            int restoreQty = detail.getQuantity();
                            product.setQty(currentQty + restoreQty);
                            System.out.println(
                                    "🔄 Restored stock for: " + product.getName() + " | +" + restoreQty + " -> "
                                            + product.getQty());
                        }
                    }

                    // Revert voucher usage
                    if (order.getVoucher() != null) {
                        try {
                            com.example.backend.entity.Voucher voucher = order.getVoucher();
                            if (voucher.getUsedCount() != null && voucher.getUsedCount() > 0) {
                                voucher.setUsedCount(voucher.getUsedCount() - 1);
                                System.out.println("✅ Voucher status restored: " + voucher.getCode());
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Failed to revert voucher usage: " + e.getMessage());
                        }
                    }

                    orderRepository.save(order);

                    result.put("status", "cancelled");
                    result.put("message", "Thanh toán đã bị hủy");
                    result.put("orderId", String.valueOf(order.getId()));
                    result.put("reason", "Người dùng hủy thanh toán hoặc thanh toán thất bại");
                }

                paymentRepository.save(payment);
            } else {
                result.put("status", "error");
                result.put("message", "Chữ ký không hợp lệ");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in handleCallback: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "error");
            result.put("message", "Lỗi xử lý callback: " + e.getMessage());
            result.put("orderId", "0");
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, String> handleIPN(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            // Verify signature
            String signValue = VNPayUtil.hashAllFields(params);
            String checkSum = VNPayUtil.hmacSHA512(vnPayConfig.getVnpHashSecret(), signValue);

            if (!checkSum.equals(vnpSecureHash)) {
                result.put("RspCode", "97");
                result.put("Message", "Invalid signature");
                return result;
            }

            String vnpTxnRef = params.get("vnp_TxnRef");
            Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef).orElse(null);

            if (payment == null) {
                result.put("RspCode", "01");
                result.put("Message", "Order not found");
                return result;
            }

            // Kiểm tra số tiền
            long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
            long expectedAmount = payment.getAmount().multiply(new BigDecimal(100)).longValue();

            if (vnpAmount != expectedAmount) {
                result.put("RspCode", "04");
                result.put("Message", "Invalid amount");
                return result;
            }

            // Kiểm tra trạng thái đã cập nhật chưa
            if ("SUCCESS".equals(payment.getPaymentStatus()) || "FAILED".equals(payment.getPaymentStatus())) {
                result.put("RspCode", "02");
                result.put("Message", "Order already confirmed");
                return result;
            }

            // Cập nhật payment
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionStatus = params.get("vnp_TransactionStatus");

            payment.setVnpResponseCode(vnpResponseCode);
            payment.setVnpTransactionStatus(vnpTransactionStatus);
            payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
            payment.setVnpBankCode(params.get("vnp_BankCode"));
            payment.setVnpBankTranNo(params.get("vnp_BankTranNo"));
            payment.setVnpCardType(params.get("vnp_CardType"));

            // Parse vnp_PayDate
            String vnpPayDate = params.get("vnp_PayDate");
            if (vnpPayDate != null && !vnpPayDate.isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    payment.setVnpPayDate(LocalDateTime.parse(vnpPayDate, formatter));
                } catch (Exception e) {
                    // Log error but continue
                }
            }

            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                payment.setPaymentStatus("SUCCESS");
                Order order = payment.getOrder();
                order.setStatus(com.example.backend.entity.enums.OrderStatus.CONFIRMED);
                order.setPaymentStatus(com.example.backend.entity.enums.PaymentStatus.PAID);
                orderRepository.save(order);
            } else {
                payment.setPaymentStatus("FAILED");
            }

            paymentRepository.save(payment);

            result.put("RspCode", "00");
            result.put("Message", "Confirm success");

        } catch (Exception e) {
            result.put("RspCode", "99");
            result.put("Message", "Unknown error: " + e.getMessage());
        }

        return result;
    }
}
