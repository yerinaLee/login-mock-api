package com.study.auth.payload.request;

public class SocialLoginRequest {
    private String provider; // "naver", "kakao", "google"
    private String providerId; // 각 소셜에서 제공하는 고유 ID
    private String email;
    private String name;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
