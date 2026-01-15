package com.example.backend.security;

import com.example.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // 🔐 Key phải >= 256 bit cho HS256
    private static final String SECRET =
            "CHANGE_ME_256_BIT_SECRET_CHANGE_ME_256_BIT_SECRET";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // ✅ Tạo token
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                ) // 1 ngày
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Lấy email từ token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Check token hợp lệ
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ===== helper =====
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
