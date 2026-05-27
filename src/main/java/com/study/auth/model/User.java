package com.study.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 로그인 ID

    @Column(unique = true, nullable = false)
    private String email;

    private String profileName; // 닉네임

    @JsonIgnore
    private String password;   // BCrypt 암호화

    // 소셜 로그인용 (일반로그인은 null)
    private String provider;   // "google", "kakao", "naver", null
    private String providerId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String email, String profileName, String password, String provider) {
        this.username = username;
        this.email = email;
        this.profileName = profileName;
        this.password = password;
        this.provider = provider;
    }
}
