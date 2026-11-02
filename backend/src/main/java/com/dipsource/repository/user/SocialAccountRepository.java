package com.dipsource.repository.user;

import com.dipsource.entity.user.SocialAccount;
import com.dipsource.entity.user.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** social_accounts 테이블 접근. */
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /** 제공자 + 소셜 고유 ID로 기존 연동을 찾는다. */
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
