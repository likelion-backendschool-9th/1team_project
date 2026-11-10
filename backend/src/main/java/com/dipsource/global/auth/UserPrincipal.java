package com.dipsource.global.auth;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** SecurityContext에 넣을 로그인 회원 정보. */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String role;

    public UserPrincipal(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security 권한 형식은 ROLE_ 접두사를 쓴다.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        // 소셜 로그인만 쓰므로 비밀번호는 없다.
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
