package com.example.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface VNPayService {

    /**
     * Tạo URL thanh toán VNPay
     */
    String createPaymentUrl(Long orderId, HttpServletRequest request) throws Exception;

    /**
     * Xử lý callback từ VNPay (Return URL)
     */
    Map<String, String> handleCallback(Map<String, String> params);

    /**
     * Xử lý IPN từ VNPay
     */
    Map<String, String> handleIPN(Map<String, String> params);
}
