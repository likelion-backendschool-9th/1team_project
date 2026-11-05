package com.dipsource.service.user;

import com.dipsource.entity.user.SocialProvider;
import java.util.Map;

public record SocialProfile(
    SocialProvider provider,
    String providerId,
    String email,
    String nickname,
    String profileImage,
    String githubUsername
) {
    public static SocialProfile of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "github" -> new SocialProfile(
                SocialProvider.GITHUB,
                String.valueOf(attributes.get("id")),
                (String) attributes.get("email"),
                (String) attributes.get("login"),
                (String) attributes.get("avatar_url"),
                (String) attributes.get("login"));
            case "google" -> new SocialProfile(
                SocialProvider.GOOGLE,
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture"),
                null);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    } 
    
}
