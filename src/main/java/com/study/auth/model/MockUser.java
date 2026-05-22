package com.study.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MockUser{
    private Long id;
    private String username;
    private String password;      // BCrypt 암호화된 값
    private String profileName;
    private String email;
    private List<String> roles;
}
