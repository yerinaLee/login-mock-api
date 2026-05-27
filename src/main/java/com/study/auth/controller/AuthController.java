package com.study.auth.controller;

import com.study.auth.Repository.RoleRepository;
import com.study.auth.Repository.UserRepository;
import com.study.auth.model.ERole;
import com.study.auth.model.Role;
import com.study.auth.model.User;
import com.study.auth.payload.request.LoginRequest;
import com.study.auth.payload.request.SignupRequest;
import com.study.auth.payload.response.JwtResponse;
import com.study.auth.payload.response.MessageResponse;
import com.study.auth.security.jwt.JwtUtils;
import com.study.auth.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    public ResponseEntity<?> login(@RequestBody LoginRequest req){

        // 1. 유저 존재 여부 확인
        if (userRepository.findByUsername(req.getUsername()).isEmpty()){
            return ResponseEntity.ok(new MessageResponse("user_notfound"));
        }

        // 2. Id/PW 인증 (내부적으로 BCrypt 비교)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // 3. SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. JWT 토큰 생성
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 5. 유저 정보 + 토큰 응답
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), userDetails.getProfileName(), roles
        ));
    }

    // api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest req){
        if (userRepository.existsByUsername(req.getUsername()))
            return ResponseEntity.badRequest().body(new MessageResponse("username_exist"));
        if (userRepository.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponse("email_exist"));

        User user = new User(
                req.getUsername(),
                req.getEmail(),
                req.getProfileName(),
                encoder.encode(req.getPassword()),
                null
        );
//        user.setUsername(req.getUsername());
//        user.setEmail(req.getEmail());
//        user.setProfileName(req.getProfileName());
//        user.setPassword(encoder.encode(req.getPassword())), // BCrypt 암호화
//        user.setProvider();


        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("role_notfound"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("success"));
    }
}
