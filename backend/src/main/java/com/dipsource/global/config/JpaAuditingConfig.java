package com.dipsource.global.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "seoulDateTimeProvider")
public class JpaAuditingConfig {

    /** created_at / updated_at은 timestamp(without time zone)라 JVM 기본 시간대와 무관하게 서울 시각을 넣는다. */
    @Bean
    DateTimeProvider seoulDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}
