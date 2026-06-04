package com.study.auth.security.jwt;

import com.study.auth.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils { // 토큰 발급 / 검증

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${loginmock.jwtSecret}")
    private String jwtSecret;

    @Value("${loginmock.jwtExpirationMs}")
    private int jwtExpirationMs;

    // 로그인 성공시 JWT 토큰 발급
    public String generateJwtToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // 페이로드 : username
                .setIssuedAt(new Date()) // 발급시각
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // 만료시각
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // HS512로 서명
                .compact();
    }
    
    // 토큰에서 username 추출
    public String getUsernameFromToken(String token){
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            logger.error("만료된 토큰 : {}", e.getMessage());
        } catch (SignatureException e){
            logger.error("서명 불일치 (위조) : {}", e.getMessage());
        } catch (Exception e){
            logger.error("토큰 오류 : {}", e.getMessage());
        }
        return false;
    }

    // 소셜 로그인용 jwt 토큰 발행
    public String generateJwtTokenFromUsername(String username){
        return Jwts.builder()
                .setSubject(username) // 페이로드 : username
                .setIssuedAt(new Date()) // 발급시각
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // 만료시각
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // HS512로 서명
                .compact();
    }


}
