package com.example.backend.controller.user;

import com.example.backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    /**
     * Tạo URL thanh toán VNPay
     * POST /api/payments/vnpay/create?orderId=123
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createPayment(
            @RequestParam Long orderId,
            HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(orderId, request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "paymentUrl", paymentUrl,
                    "message", "Tạo URL thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * Callback từ VNPay (Return URL)
     * GET /api/payments/vnpay/callback?vnp_Amount=...&vnp_BankCode=...
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> vnpayCallback(@RequestParam Map<String, String> params) {
        Map<String, String> result = vnPayService.handleCallback(params);

        String status = result.get("status");
        String message = result.get("message");
        String orderId = result.get("orderId");
        String reason = result.get("reason");

        boolean isSuccess = "success".equals(status);
        boolean isCancelled = "cancelled".equals(status);

        String statusColor = isSuccess ? "#10b981" : "#ef4444";
        String statusIcon = isSuccess ? "✓" : "✗";
        String statusText = isSuccess ? "Thanh toán thành công!"
                : isCancelled ? "Thanh toán đã bị hủy!" : "Thanh toán thất bại!";

        // Build deep link URL
        String deepLinkUrl;
        if (isSuccess) {
            deepLinkUrl = "didong2://payment/result?status=success&orderId=" + orderId;
        } else {
            deepLinkUrl = "didong2://payment/result?status=failed&orderId=" + orderId +
                    (reason != null
                            ? "&reason=" + java.net.URLEncoder.encode(reason, java.nio.charset.StandardCharsets.UTF_8)
                            : "");
        }

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Kết quả thanh toán</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                        }
                        .container {
                            background: white;
                            border-radius: 20px;
                            padding: 40px;
                            max-width: 500px;
                            width: 100%%;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                            text-align: center;
                            animation: slideUp 0.5s ease-out;
                        }
                        @keyframes slideUp {
                            from {
                                opacity: 0;
                                transform: translateY(30px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }
                        .icon {
                            width: 80px;
                            height: 80px;
                            border-radius: 50%%;
                            background: %s;
                            color: white;
                            font-size: 48px;
                            line-height: 80px;
                            margin: 0 auto 20px;
                            animation: scaleIn 0.5s ease-out 0.2s both;
                        }
                        @keyframes scaleIn {
                            from {
                                transform: scale(0);
                            }
                            to {
                                transform: scale(1);
                            }
                        }
                        h1 {
                            color: #1f2937;
                            font-size: 28px;
                            margin-bottom: 10px;
                        }
                        .message {
                            color: #6b7280;
                            font-size: 16px;
                            margin-bottom: 20px;
                            line-height: 1.5;
                        }
                        .order-info {
                            background: #f3f4f6;
                            border-radius: 12px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .order-info p {
                            color: #4b5563;
                            font-size: 14px;
                            margin: 8px 0;
                        }
                        .order-info strong {
                            color: #1f2937;
                        }
                        .countdown {
                            color: #6b7280;
                            font-size: 14px;
                            margin: 20px 0;
                        }
                        .countdown span {
                            color: %s;
                            font-weight: bold;
                            font-size: 18px;
                        }
                        .btn {
                            display: inline-block;
                            background: %s;
                            color: white;
                            padding: 14px 32px;
                            border-radius: 12px;
                            text-decoration: none;
                            font-weight: 600;
                            font-size: 16px;
                            transition: all 0.3s ease;
                            border: none;
                            cursor: pointer;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                        }
                        .btn:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 20px rgba(0,0,0,0.2);
                        }
                        .btn:active {
                            transform: translateY(0);
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="icon">%s</div>
                        <h1>%s</h1>
                        <p class="message">%s</p>

                        <div class="order-info">
                            <p><strong>Mã đơn hàng:</strong> #%s</p>
                        </div>

                        <p class="countdown">
                            Tự động quay về app sau <span id="countdown">3</span> giây...
                        </p>

                        <a href="%s" class="btn" onclick="closeWindow()">
                            Quay về ứng dụng
                        </a>
                    </div>

                    <script>
                        let seconds = 3;
                        const countdownEl = document.getElementById('countdown');

                        const timer = setInterval(() => {
                            seconds--;
                            countdownEl.textContent = seconds;

                            if (seconds <= 0) {
                                clearInterval(timer);
                                closeWindow();
                            }
                        }, 1000);

                        function closeWindow() {
                            // Try to redirect to deep link
                            window.location.href = '%s';

                            // Try to close the window after a short delay
                            setTimeout(() => {
                                window.close();

                                // If window.close() doesn't work, try to go back
                                setTimeout(() => {
                                    if (!window.closed) {
                                        window.history.back();
                                    }
                                }, 100);
                            }, 500);
                        }
                    </script>
                </body>
                </html>
                """
                .formatted(
                        statusColor, statusColor, statusColor,
                        statusIcon, statusText, message,
                        orderId, deepLinkUrl, deepLinkUrl);

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    /**
     * IPN từ VNPay (Server-to-Server)
     * GET /api/payments/vnpay/ipn?vnp_Amount=...&vnp_BankCode=...
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<?> vnpayIPN(@RequestParam Map<String, String> params) {
        Map<String, String> result = vnPayService.handleIPN(params);
        return ResponseEntity.ok(result);
    }
}
