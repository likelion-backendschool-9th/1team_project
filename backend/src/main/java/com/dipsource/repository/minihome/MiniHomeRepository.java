package com.dipsource.repository.minihome;

import com.dipsource.entity.minihome.MiniHome;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** mini_homes 테이블 접근. */
public interface MiniHomeRepository extends JpaRepository<MiniHome, Long> {

    Optional<MiniHome> findByUserId(Long userId);
}
