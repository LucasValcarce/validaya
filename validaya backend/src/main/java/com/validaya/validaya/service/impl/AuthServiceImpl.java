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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthDto.IdentifyResponse identify(String identification) {
        User user = userRepository.findByIdentification(identification).orElse(null);
        
        AuthDto.IdentifyResponse response = new AuthDto.IdentifyResponse();
        if (user != null) {
            response.setExists(true);
            response.setUserId(user.getId());
            response.setFullName(user.getFullName());
            response.setMessage("Usuario encontrado");
        } else {
            response.setExists(false);
            response.setMessage("Usuario no encontrado");
        }
        return response;
    }

    @Override
    public AuthDto.FaceVerificationResponse verifyFace(String identification, String faceBase64) {
        User user = userRepository.findByIdentification(identification).orElse(null);
        
        AuthDto.FaceVerificationResponse response = new AuthDto.FaceVerificationResponse();
        
        if (user == null) {
            response.setVerified(false);
            response.setMessage("Usuario no encontrado");
            return response;
        }

        // Verificar que el usuario tiene cara registrada
        if (user.getFaceHash() == null || !Boolean.TRUE.equals(user.getFaceVerified())) {
            response.setVerified(false);
            response.setMessage("El usuario no tiene rostro registrado o no ha completado el enrollment");
            return response;
        }

        // Comparar el hash del rostro enviado con el almacenado
        try {
            byte[] faceBytes = Base64.getDecoder().decode(faceBase64.getBytes(StandardCharsets.UTF_8));
            String faceHash = sha256(faceBytes);

            if (faceHash.equals(user.getFaceHash())) {
                // Rostro coincide: generar JWT temporal
                UserPrincipal principal = UserPrincipal.from(user);
                String token = jwtTokenProvider.createToken(principal);

                response.setVerified(true);
                response.setToken(token);
                response.setUserId(user.getId());
                response.setEmail(user.getEmail());
                response.setFullName(user.getFullName());
                response.setUserType(user.getUserType() != null ? user.getUserType().name() : null);
                response.setMessage("Rostro verificado correctamente");

                log.info("Usuario {} verificado por rostro", user.getId());
            } else {
                response.setVerified(false);
                response.setMessage("El rostro no coincide");
            }
        } catch (IllegalArgumentException e) {
            response.setVerified(false);
            response.setMessage("Formato de imagen inválido");
        }

        return response;
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse setPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Establecer contraseña
        user.setPasswordHash(passwordEncoder.encode(password));
        user = userRepository.save(user);

        // Generar token final
        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtTokenProvider.createToken(principal);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        response.setPasswordSet(true);

        log.info("Contraseña establecida para usuario {}", userId);
        return response;
    }

    @Override
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // Buscar usuario por identificación
        User user = userRepository.findByIdentification(request.getIdentification())
                .orElseThrow(() -> new BadCredentialsException("Identificación o contraseña inválida"));

        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Identificación o contraseña inválida");
        }

        // Actualizar último login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Generar token
        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtTokenProvider.createToken(principal);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        response.setPasswordSet(true);

        log.info("Login exitoso para usuario {}", user.getId());
        return response;
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        // Registro está deshabilitado; los usuarios se crean a través del flujo de enrollment facial
        throw new UnsupportedOperationException("El registro público está deshabilitado. Use el flujo de enrollment facial.");
    }

    @Override
    public void logout(String token) {
        log.info("Logout solicitado");
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}