package com.dipsource.repository.user;

import com.dipsource.entity.user.UserSanction;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    List<UserSanction> findByUser_IdOrderByCreatedAtDesc(Long userId);
    
      /** 아직 해제하지 않은 제재. 여러 건이면 가장 최근 것. */
    Optional<UserSanction> findFirstByUser_IdAndReleasedAtIsNullOrderByCreatedAtDesc(Long userId);
}
