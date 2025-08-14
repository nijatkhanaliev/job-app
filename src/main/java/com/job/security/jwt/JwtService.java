package com.job.security.jwt;

import com.job.config.cache.RedisTokenService;
import com.job.dao.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RedisTokenService redisTokenService;

    @Value("${application.jwt.secret-key-string}")
    private String secretkeyString;

    @Value("${application.jwt.refresh-token-expiration}")
    private Long accessExpirationMs;

    @Value("${application.jwt.access-token-expiration}")
    private Long refreshExpirationMs;

    private SecretKey secretKey;


    @PostConstruct
    void init() {
        byte[] bytes = Decoders.BASE64.decode(secretkeyString);
        this.secretKey = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getUserRole());
        claims.put("type", "access");

        String accessToken = buildToken(user.getEmail(), claims, accessExpirationMs);
        redisTokenService.save(user.getEmail() + ":access", user.getEmail(), accessExpirationMs);

        return accessToken;
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getUserRole());
        claims.put("type", "refresh");

        String accessToken = buildToken(user.getEmail(), claims, refreshExpirationMs);
        redisTokenService.save(user.getEmail() + ":refresh", user.getEmail(), refreshExpirationMs);

        return accessToken;
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(token);

        return claimsResolver.apply(claims);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return Objects.equals(extractUserEmail(token), userDetails.getUsername()) &&
                !isTokenExpired(token) &&
                redisTokenService.isValid(userDetails.getUsername() + ":access", token);
    }

    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String key = email + ":refresh";

        return redisTokenService.isValid(key, refreshToken);
    }

    public void invalidateToken(String email) {
        redisTokenService.invalidateAccessToken(email);
        redisTokenService.invalidateRefreshToken(email);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private String buildToken(String email, Map<String, Object> claims, Long accessExpirationMs) {
        return Jwts.builder()
                .signWith(secretKey)
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .compact();
    }

}
