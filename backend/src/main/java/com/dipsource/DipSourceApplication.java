package com.dipsource;

import com.dipsource.global.auth.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class) // app.jwt 설정을 빈으로 등록
public class DipSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DipSourceApplication.class, args);
    }
}
