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
            case "kakao" -> {
                Map<String, Object> account = nestedMap(attributes, "kakao_account");
                Map<String, Object> profile = nestedMap(account, "profile");
                yield new SocialProfile(
                SocialProvider.KAKAO,
                String.valueOf(attributes.get("id")),
                text(account, "email"),
                text(profile, "nickname"),
                text(profile, "profile_image_url"),
                null);
            }
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    } 

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source == null ? null : source.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static String text(Map<String, Object> source, String key) {
        Object value = source == null ? null : source.get(key);
        return value == null ? null : String.valueOf(value);
    }
    
}
