package com.study.auth.security.jwt;

import com.study.auth.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils { // 토큰 발급 / 검증

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String JWT_SECRET = "studySecretKey1234567890";  // 실제론 환경변수로 관리
    private final int JWT_EXPIRATION_MS = 86400000; // 24시간

    // 토큰 발급
    public String generateToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // 페이로드 : username
                .setIssuedAt(new Date()) // 발급시각
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS)) // 만료시각
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET) // 서명
                .compact();
    }
    
    // 토큰에서 username 추출
    public String getUsernameFromToken(String token){
        return Jwts.parser().setSigningKey(JWT_SECRET)
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            logger.error("만료된 토큰 : {}", e.getMessage());
        }

        return false;
    }


}
