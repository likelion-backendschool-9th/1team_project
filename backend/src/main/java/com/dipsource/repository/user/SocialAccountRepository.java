package com.dipsource.repository.user;

import com.dipsource.entity.user.SocialAccount;
import com.dipsource.entity.user.enums.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** social_accounts 테이블 접근. */
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * 제공자 + 소셜 고유 ID로 기존 연동을 찾는다.
     * user는 LAZY라서 함께 읽어 온다. 안 그러면 트랜잭션 밖(로그인 성공 핸들러)에서 role을 읽을 때 예외가 난다.
     */
    @EntityGraph(attributePaths = "user")
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
