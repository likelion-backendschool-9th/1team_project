package com.dipsource.service.user.impl;

import com.dipsource.entity.user.User;
import com.dipsource.entity.user.enums.UserStatus;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.coin.CoinService;
import com.dipsource.service.user.PlusSubscriptionService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** {@link PlusSubscriptionService} 구현체. */
@Slf4j
@Service
public class PlusSubscriptionServiceImpl implements PlusSubscriptionService {

    /** 프론트 policy.ts의 PLUS_PRICE_WON과 같다. */
    static final int PRICE_KRW = 4900;
    /** 한 번에 따라잡는 최대 횟수. 30일 × 24 = 2년. 더 밀렸으면 다음 실행이 이어서 한다. */
    static final int MAX_PERIODS = 24;
    static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final CoinService coinService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    @Autowired
    public PlusSubscriptionServiceImpl(
            UserRepository userRepository,
            CoinService coinService,
            PlatformTransactionManager transactionManager
    ) {
        this(userRepository, coinService, new TransactionTemplate(transactionManager), Clock.system(ZONE));
    }

    PlusSubscriptionServiceImpl(
            UserRepository userRepository,
            CoinService coinService,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.coinService = coinService;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void start(Long userId) {
        User user = findUser(userId);
        user.startPlus(LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public void cancel(Long userId) {
        User user = findUser(userId);
        user.cancelPlus();
    }

    @Override
    public void renewDue() {
        LocalDateTime now = LocalDateTime.now(clock);
        for (Long userId : userRepository.findPlusRenewalDueIds(now)) {
            try {
                transactionTemplate.executeWithoutResult(status -> renewUser(userId, now));
            } catch (RuntimeException ex) {
                log.warn("딥소스 플러스 자동 갱신 실패 userId={}", userId, ex);
            }
        }
    }

    /** 한 회원을 잠그고, 만료 시각이 지금 이후가 될 때까지 30일씩 연장하며 가상 결제한다. */
    void renewUser(Long userId, LocalDateTime now) {
        User user = userRepository.findByIdForUpdate(userId).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE || !user.isPlusAutoRenew()) {
            return;
        }
        int periods = 0;
        while (user.getPlusUntil() != null && !user.getPlusUntil().isAfter(now) && periods < MAX_PERIODS) {
            user.extendPlus();
            coinService.payPlus(userId, PRICE_KRW);
            periods++;
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
    }
}
