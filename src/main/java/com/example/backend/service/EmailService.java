package com.example.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("E-com Team <support@e-com.com>");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Mã xác thực quên mật khẩu";
        String body = "Mã xác thực của bạn là: " + otp + "\n" +
                "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.";
        sendEmail(to, subject, body);
    }
}
