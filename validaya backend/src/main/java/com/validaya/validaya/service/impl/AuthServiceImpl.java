package com.validaya.validaya.service.impl;

import com.validaya.validaya.config.JwtTokenProvider;
import com.validaya.validaya.config.security.UserPrincipal;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.dto.AuthDto;
import com.validaya.validaya.entity.enums.UserType;
import com.validaya.validaya.repository.UserRepository;
import com.validaya.validaya.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

            // Update last login
            userRepository.findById(principal.getId()).ifPresent(u -> {
                u.setLastLoginAt(java.time.LocalDateTime.now());
                userRepository.save(u);
            });

            String token = jwtTokenProvider.createToken(principal);

            AuthDto.AuthResponse response = new AuthDto.AuthResponse();
            response.setToken(token);
            response.setUserId(principal.getId());
            response.setEmail(principal.getUsername());
            response.setUserType(principal.getAuthorities().stream().findFirst().map(Object::toString).orElse(null));
            return response;
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByIdentification(request.getIdentification())) {
            throw new IllegalArgumentException("La identificación ya está registrada");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .identification(request.getIdentification())
                .phone(request.getPhone())
                .userType(UserType.citizen)
                .isActive(true)
                .faceVerified(false)
                .build();

        user = userRepository.save(user);

        // Auto-login after register
        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtTokenProvider.createToken(principal);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        return response;
    }

    @Override
    public void logout(String token) {
        // Stateless JWT: logout is client-side unless you implement a blacklist.
        log.info("Logout requested");
    }
}