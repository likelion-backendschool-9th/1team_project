package com.dipsource.service.user.impl;

import com.dipsource.entity.user.User;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.repository.user.UserRepository;
import com.dipsource.service.user.PlusSubscriptionService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link PlusSubscriptionService} 구현체. */
@Service
public class PlusSubscriptionServiceImpl implements PlusSubscriptionService {

    static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public PlusSubscriptionServiceImpl(UserRepository userRepository) {
        this(userRepository, Clock.system(ZONE));
    }

    PlusSubscriptionServiceImpl(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
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

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없어요."));
    }
}
