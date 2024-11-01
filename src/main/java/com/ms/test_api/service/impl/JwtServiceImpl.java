package com.ms.test_api.service.impl;

import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ms.test_api.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.security.*;

@Service
public class JwtServiceImpl implements JwtService {

    @Override
    public String generateToken(UserDetails user) {
        return generateToken(new HashMap<>(), user);
    }
    
    @Override
    public String generateRefreshToken(UserDetails user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails) {

        String roles = userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority()).collect(Collectors.joining(" "));

        claims.put("scope", roles);

        return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .signWith(getKey(), SignatureAlgorithm.HS256)
                    .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14))
                    .signWith(getKey(), SignatureAlgorithm.HS256)
                    .compact();
    }


    private Key getKey(){
        byte[] keyByte = Decoders.BASE64.decode("fc118fcb27d87e7053d0a3bcc6ba2eb47b5526e5a35a2cb9433f2df6cc677ccf");
        return Keys.hmacShaKeyFor(keyByte);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extraAllClaim(token);
        return claimResolver.apply(claims);
    }
    
    private Claims extraAllClaim(String token) {
        return Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }
    
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    
}
