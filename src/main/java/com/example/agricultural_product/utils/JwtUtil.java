package com.example.agricultural_product.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

public class JwtUtil {

    private static final String SECRET_KEY = "A9fT7zQpX3LmC2NvY8HrJ6KsB5WdE4Ty";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1天

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 生成 Token
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 Token
    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 校验 Token 是否有效
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
