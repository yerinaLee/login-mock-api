package com.study.auth.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String profileName;
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, String profileName, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.profileName = profileName;
        this.roles = roles;
    }

    // 프론트가 받는 JSON:
    // {
    //   "token": "eyJhbGci...",
    //   "type": "Bearer",
    //   "id": 1,
    //   "username": "user123",
    //   "profileName": "홍길동",
    //   "email": "user@test.com",
    //   "roles": ["ROLE_USER"]
    // }

}
