package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.dto.AuthDto;
import com.validaya.validaya.repository.UserRepository;
import com.validaya.validaya.service.BiometricsService;
import com.validaya.validaya.config.JwtTokenProvider;
import com.validaya.validaya.config.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BiometricsServiceImpl implements BiometricsService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Simple in-memory session storage for enrollment sessions (session -> identification)
    private final Map<String, String> enrollSessions = new ConcurrentHashMap<>();

    @Override
    public String initEnrollment(String identification, String fullName) {
        String session = UUID.randomUUID().toString();
        enrollSessions.put(session, identification);

        // If user exists, set enrollment status pending
        userRepository.findByIdentification(identification).ifPresent(u -> {
            u.setEnrollmentStatus("pending");
            userRepository.save(u);
        });

        return session;
    }

    @Override
    public boolean submitFace(String sessionToken, String faceBase64) {
        String ident = enrollSessions.get(sessionToken);
        if (ident == null) return false;

        byte[] bytes = Base64.getDecoder().decode(faceBase64.getBytes(StandardCharsets.UTF_8));
        String hash = sha256(bytes);

        // Buscar usuario por identificación
        User user = userRepository.findByIdentification(ident).orElse(null);
        
        // Si no existe, crear un usuario base (ciudadano, sin contraseña)
        if (user == null) {
            user = User.builder()
                    .identification(ident)
                    .email("user_" + ident + "@validaya.local") // Email temporal
                    .fullName(ident) // Será actualizado después
                    .userType(com.validaya.validaya.entity.enums.UserType.citizen)
                    .passwordHash("") // Sin contraseña inicialmente
                    .isActive(true)
                    .faceVerified(false)
                    .build();
            user = userRepository.save(user);
        }

        // Actualizar datos faciales
        user.setFaceVectorEncrypted(bytes);
        user.setFaceHash(hash);
        user.setEnrollmentStatus("verified");
        user.setFaceVerified(true);
        userRepository.save(user);
        
        enrollSessions.remove(sessionToken);
        return true;
    }

    @Override
    public AuthDto.AuthResponse authenticateByFace(String faceBase64) {
        byte[] bytes = Base64.getDecoder().decode(faceBase64.getBytes(StandardCharsets.UTF_8));
        String hash = sha256(bytes);

        for (User u : userRepository.findAll()) {
            if (u.getFaceHash() != null && u.getFaceHash().equals(hash) && Boolean.TRUE.equals(u.getFaceVerified())) {
                // Generate JWT token
                String token = jwtTokenProvider.createToken(UserPrincipal.from(u));
                AuthDto.AuthResponse resp = new AuthDto.AuthResponse();
                resp.setToken(token);
                resp.setUserId(u.getId());
                resp.setEmail(u.getEmail());
                resp.setFullName(u.getFullName());
                resp.setUserType(u.getUserType() != null ? u.getUserType().name() : null);
                return resp;
            }
        }
        return null;
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
