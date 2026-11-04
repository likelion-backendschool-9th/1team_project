package com.dipsource.entity.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTagProfileTest {

    @Test
    @DisplayName("가중치 감소 시 0 미만으로 내려가지 않는다")
    void weightFloorIsZero() {
        User user = User.builder().email("a@test.local").nickname("a").build();
        UserTagProfile profile = new UserTagProfile(user, 1L, 1);

        profile.addWeight(-3);

        assertThat(profile.getWeight()).isZero();
    }
}
