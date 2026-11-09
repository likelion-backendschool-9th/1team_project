package com.dipsource.service.admin;

import com.dipsource.entity.user.enums.UserRole;
import com.dipsource.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버가 뜰 때 MASTER_EMAIL 회원을 최고 관리자(MASTER)로 만든다.
 * 마스터는 화면에서 줄 수 없어서, 처음 한 명은 이 설정으로 정한다.
 * 해당 이메일로 한 번 가입(소셜 로그인)한 뒤 서버를 다시 띄우면 적용된다. 비어 있으면 아무것도 하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MasterAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${MASTER_EMAIL:}")
    private String masterEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (masterEmail == null || masterEmail.isBlank()) {
            return;
        }
        userRepository.findByEmail(masterEmail.strip()).ifPresentOrElse(
                user -> {
                    if (user.getRole() != UserRole.MASTER) {
                        user.changeRole(UserRole.MASTER);
                        log.info("MASTER_EMAIL 회원을 마스터로 지정했습니다 userId={}", user.getId());
                    }
                },
                () -> log.warn("MASTER_EMAIL 회원이 아직 없습니다. 이 이메일로 가입한 뒤 서버를 다시 띄우면 마스터가 됩니다"));
    }
}
