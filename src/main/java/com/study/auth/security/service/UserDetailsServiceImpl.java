package com.study.auth.security.service;

import com.study.auth.model.MockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private List<MockUser> users;

    // 앱 시작 시 인메모리 유저 초기화
    @PostConstruct
    public void init(){
        users = List.of(
                new MockUser(1L, "hong",  passwordEncoder.encode("1234"),  "홍길동", "hong@test.com",  List.of("ROLE_USER")),
                new MockUser(2L, "admin", passwordEncoder.encode("admin"), "관리자", "admin@test.com", List.of("ROLE_ADMIN"))
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MockUser user = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return UserDetailsImpl.build(user);
    }
}
