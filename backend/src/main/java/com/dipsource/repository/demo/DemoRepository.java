package com.dipsource.repository.demo;

import com.dipsource.entity.demo.Demo;
import org.springframework.data.jpa.repository.JpaRepository;

/** demos 테이블 접근 */
public interface DemoRepository extends JpaRepository<Demo, Long> {
}
