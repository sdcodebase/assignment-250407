package com.sdcodebase.assignment.auth.application;

import com.sdcodebase.assignment.auth.application.dto.LoginRequest;
import com.sdcodebase.assignment.auth.application.dto.SignUpRequest;
import com.sdcodebase.assignment.auth.application.dto.TokenResponse;
import com.sdcodebase.assignment.auth.domain.EmailAlreadyExistsException;
import com.sdcodebase.assignment.auth.domain.InvalidCredentialsException;
import com.sdcodebase.assignment.auth.domain.event.UserLoggedInEvent;
import com.sdcodebase.assignment.auth.domain.event.UserSignedUpEvent;
import com.sdcodebase.assignment.user.domain.Role;
import com.sdcodebase.assignment.user.domain.User;
import com.sdcodebase.assignment.user.domain.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                Role.MEMBER
        );
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.createToken(saved.getId(), saved.getEmail(), saved.getRole());
        // analytics 컨텍스트가 AFTER_COMMIT 시점에 활동 로그로 적재한다.
        eventPublisher.publishEvent(new UserSignedUpEvent(saved.getId()));
        return TokenResponse.bearer(token);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getRole());
        // 로그인 성공 이벤트만 발행한다. 실패 케이스는 위에서 예외로 종료된다.
        eventPublisher.publishEvent(new UserLoggedInEvent(user.getId()));
        return TokenResponse.bearer(token);
    }
}
