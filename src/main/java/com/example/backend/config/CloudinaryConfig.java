package com.example.backend.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dh42pzbxa"); // Thay bằng tên cloud của bạn
        config.put("api_key", "276569191544755");       // Thay bằng API Key
        config.put("api_secret", "GWWOotJlJmqwY5o01gaD6pHSkXQ"); // Thay bằng API Secret
        return new Cloudinary(config);
    }
}