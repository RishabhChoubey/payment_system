package com.paypal.transaction_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtil {
    public static Long extractUserId(String token, String secretKey) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
        // Assumes userId is stored as a claim named "userId" (adjust as needed)
        return claims.get("userId", Long.class);
    }
}

