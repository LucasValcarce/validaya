package com.validaya.validaya.service.impl;

import com.validaya.validaya.config.JwtTokenProvider;
import com.validaya.validaya.config.security.UserPrincipal;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.dto.AuthDto;
import com.validaya.validaya.entity.enums.UserType;
import com.validaya.validaya.integracion.FacialRecognitionService;
import com.validaya.validaya.integracion.dtos.FacialVerifyResponse;
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
    private final FacialRecognitionService facialRecognitionService;

    @Override
    public AuthDto.IdentifyResponse identify(String identification, String faceBase64) {
        AuthDto.IdentifyResponse response = new AuthDto.IdentifyResponse();
        
        // Paso 1: Verificar que el usuario existe
        User user = userRepository.findByIdentification(identification).orElse(null);
        
        if (user == null) {
            response.setExists(false);
            response.setVerified(false);
            response.setMessage("Usuario no encontrado");
            log.warn("Intento de identificación con CI inexistente: {}", identification);
            return response;
        }
        
        response.setExists(true);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType() != null ? user.getUserType().name() : null);
        
        // Paso 2: Verificar rostro usando el servicio de modelado facial
        try {
            FacialVerifyResponse facialResponse = facialRecognitionService.verifyFace(
                    identification, 
                    faceBase64
            );
            
            if (facialResponse.isSuccess() && facialResponse.isMatch()) {
                // Rostro verificado correctamente
                response.setVerified(true);
                response.setConfidence(facialResponse.getConfidence());
                response.setMessage("Identidad verificada correctamente");
                
                // Generar JWT temporal (válido para establecer contraseña)
                UserPrincipal principal = UserPrincipal.from(user);
                String token = jwtTokenProvider.createToken(principal);
                response.setToken(token);
                
                log.info("Usuario {} identificado exitosamente por rostro (confianza: {}%)", 
                        user.getId(), 
                        facialResponse.getConfidence() * 100);
            } else {
                // Rostro no coincide
                response.setVerified(false);
                response.setConfidence(facialResponse.getConfidence());
                response.setMessage(facialResponse.getMessage() != null ? 
                        facialResponse.getMessage() : "El rostro no coincide con el registrado");
                
                log.warn("Verificación facial fallida para usuario {}: {}", 
                        user.getId(), response.getMessage());
            }
        } catch (Exception e) {
            log.error("Error al verificar rostro para usuario {}: {}", 
                    user.getId(), e.getMessage(), e);
            response.setVerified(false);
            response.setMessage("Error al verificar rostro: " + e.getMessage());
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
        // Paso 1: Buscar usuario por identificación
        User user = userRepository.findByIdentification(request.getIdentification())
                .orElseThrow(() -> new BadCredentialsException("Identificación o contraseña inválida"));

        // Paso 2: Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Intento de login con contraseña incorrecta para usuario {}", user.getId());
            throw new BadCredentialsException("Identificación o contraseña inválida");
        }

        // Paso 3: Verificar rostro usando el servicio de modelado facial
        try {
            FacialVerifyResponse facialResponse = facialRecognitionService.verifyFace(
                    request.getIdentification(),
                    request.getFaceBase64()
            );
            
            if (!facialResponse.isSuccess() || !facialResponse.isMatch()) {
                log.warn("Login fallido: verificación facial rechazada para usuario {} (confianza: {}%)",
                        user.getId(),
                        facialResponse.getConfidence() * 100);
                throw new BadCredentialsException(
                        facialResponse.getMessage() != null ? 
                        facialResponse.getMessage() : "Verificación facial fallida"
                );
            }
            
            log.info("Login exitoso: contraseña y rostro verificados para usuario {} (confianza: {}%)",
                    user.getId(),
                    facialResponse.getConfidence() * 100);
        } catch (RuntimeException e) {
            // Re-lanzar excepciones de BadCredentialsException
            if (e instanceof BadCredentialsException) {
                throw e;
            }
            // Para otras excepciones (conexión fallida, etc)
            log.error("Error al verificar rostro en login para usuario {}: {}", 
                    user.getId(), e.getMessage(), e);
            throw new BadCredentialsException("Error al verificar rostro: " + e.getMessage());
        }

        // Actualizar último login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Generar token de sesión
        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtTokenProvider.createToken(principal);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        response.setPasswordSet(true);

        return response;
    }

    @Override
    public void logout(String token) {
        log.info("Logout solicitado");
    }
}