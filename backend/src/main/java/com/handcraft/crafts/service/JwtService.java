package com.handcraft.crafts.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    private static final String SECRET_KEY = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";
    private static final String ROLES_CLAIM = "roles";

    // ✅ 1. Generate token with roles
    public String generateToken(String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, roles);
        return createToken(claims, email);
    }

    // ✅ 2. Generate token with only email (default role use-case)
    public String generateToken(String email) {
        return generateToken(email, List.of("ROLE_USER")); // default role
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 mins validity
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 🔍 Extract roles from token as List<String>
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get(ROLES_CLAIM);
        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        } else if (rolesObject instanceof String) {
            // In case roles were saved as comma separated string
            return Arrays.stream(((String) rolesObject).split(","))
                    .map(String::trim)
                    .toList();
        }
        return Collections.emptyList();
    }
}
