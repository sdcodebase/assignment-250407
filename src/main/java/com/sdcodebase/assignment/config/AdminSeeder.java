package com.sdcodebase.assignment.config;

import com.sdcodebase.assignment.user.domain.Role;
import com.sdcodebase.assignment.user.domain.User;
import com.sdcodebase.assignment.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 시연 편의를 위해 기동 시점에 관리자 계정을 자동 생성한다. 이미 존재하면 건너뛴다.
 * 운영 환경에서는 별도 마이그레이션/관리 도구로 대체되어야 한다.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:admin@example.com}") String email,
                       @Value("${app.admin.password:admin1234}") String password,
                       @Value("${app.admin.name:관리자}") String name) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(new User(email, passwordEncoder.encode(password), name, Role.ADMIN));
        log.info("Seeded admin account: {}", email);
    }
}
