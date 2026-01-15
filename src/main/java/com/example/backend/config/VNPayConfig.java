package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.api.url}")
    private String vnpApiUrl;

    @Value("${vnpay.tmn.code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash.secret}")
    private String vnpHashSecret;

    @Value("${vnpay.return.url}")
    private String vnpReturnUrl;

    @Value("${vnpay.version}")
    private String vnpVersion;

    public String getVnpApiUrl() {
        return vnpApiUrl;
    }

    public String getVnpTmnCode() {
        return vnpTmnCode;
    }

    public String getVnpHashSecret() {
        return vnpHashSecret;
    }

    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }

    public String getVnpVersion() {
        return vnpVersion;
    }
}
