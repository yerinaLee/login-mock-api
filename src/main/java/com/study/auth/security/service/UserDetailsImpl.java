package com.study.auth.security.service;

import com.study.auth.model.MockUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class UserDetailsImpl implements UserDetails { // Spring Security가 인식하는 유저 객체

    private Long id;
    private String username;
    private String password;
    private String profileName;
    private Collection<? extends GrantedAuthority> authorities;

    // MockUser -> UserDetailsImpl 변환
    public static UserDetailsImpl build(MockUser user){
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getId(), user.getUsername(), user.getPassword(),
                user.getProfileName(), authorities);
    }

    public Long getId() {return id ;}
    public String getProfileName() {return profileName;}

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}
