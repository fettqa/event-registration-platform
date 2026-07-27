package com.fettqa.events.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + properties.expirationMs());

    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("role", user.getRole().name())
        .claim("uid", user.getId())
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(key)
        .compact();
  }

  public String extractEmail(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isValid(String token, String email) {
    Claims claims = parseClaims(token);
    boolean emailMatches = claims.getSubject().equalsIgnoreCase(email);
    boolean notExpired = claims.getExpiration().after(new Date());
    return emailMatches && notExpired;
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
