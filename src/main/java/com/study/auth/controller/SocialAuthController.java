package com.study.auth.controller;

import com.study.auth.Repository.RoleRepository;
import com.study.auth.Repository.UserRepository;
import com.study.auth.model.ERole;
import com.study.auth.model.Role;
import com.study.auth.model.User;
import com.study.auth.payload.request.SocialLoginRequest;
import com.study.auth.payload.response.JwtResponse;
import com.study.auth.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
public class SocialAuthController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder encoder;

    // ✅ POST /api/social/login
    // 프론트에서 소셜 SDK로 받은 정보를 백엔드로 전달
    @PostMapping("/login")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginRequest req){
        // req: { provider, providerId, email, name }

        // 1. 이미 가입된 소셜 유저인지 확인
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(req.getProvider(), req.getProviderId());

        User user;
        if (existingUser.isPresent()){ // != null
            // 기존 유저 → 그냥 로그인
            user = existingUser.get();
        } else {
            // 신규 유저 -> 자동 회원가입
            user = new User(
                    req.getProvider()+"_"+req.getProviderId(), // 고유 ID
                    req.getEmail() != null ? req.getEmail() : req.getProvider() + "_" + req.getProvider() + "@social.com",
                    req.getName(),
                    encoder.encode(UUID.randomUUID().toString()),
                    req.getProvider()
            );

            user.setProviderId(req.getProviderId());

            Role role = roleRepository.findByName(ERole.ROLE_USER).orElseThrow();
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }


        // 2. JWT 직접 생성 (소셜은 AuthenticationManager 거치지 않음)
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getUsername());

        return ResponseEntity.ok(new JwtResponse(
                jwt, user.getId(), user.getUsername(), user.getEmail(), user.getProfileName(), List.of("ROLE_USER")
        ));
    }
}
