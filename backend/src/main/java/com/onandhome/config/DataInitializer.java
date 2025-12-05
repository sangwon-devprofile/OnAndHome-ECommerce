package com.onandhome.config;

import com.onandhome.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    
    /**
     * 애플리케이션 시작 시 초기 데이터 설정
     * 회원가입으로만 사용자 생성 가능
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("애플리케이션 시작 - 데이터베이스 준비 완료");
            log.info("회원가입으로 새로운 사용자를 생성해주세요.");
        };
    }
}
