package com.validaya.validaya.controller;

import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.AuthDto;
import com.validaya.validaya.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/identify")
    public ResponseEntity<ApiResponse<AuthDto.IdentifyResponse>> identify(
            @Valid @RequestBody AuthDto.IdentifyRequest request) {
        
        AuthDto.IdentifyResponse response = authService.identify(
                request.getIdentification(),
                request.getFaceBase64()
        );
        
        if (!response.isExists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado"));
        }
        
        if (!response.isVerified()) {
            //return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            //        .body(ApiResponse.error(response.getMessage()));
            return ResponseEntity.ok(ApiResponse.ok("Error con el servicio, pero se esta permiteindo acceso", response));
        }
        
        return ResponseEntity.ok(ApiResponse.ok("Identidad verificada", response));
    }

    @PutMapping("/set-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> setPassword(
            @Valid @RequestBody AuthDto.SetPasswordRequest request) {
        Long userId = getCurrentUserId();
        AuthDto.AuthResponse response = authService.setPassword(userId, request.getPassword());
        
        return ResponseEntity.ok(ApiResponse.ok("Contraseña establecida correctamente", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        
        //try {
            AuthDto.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
        //} catch (org.springframework.security.authentication.BadCredentialsException e) {
        //    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //            .body(ApiResponse.error(e.getMessage()));
        //}
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        authService.logout(token);
        
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada", null));
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof com.validaya.validaya.config.security.UserPrincipal) {
            com.validaya.validaya.config.security.UserPrincipal principal = 
                (com.validaya.validaya.config.security.UserPrincipal) auth.getPrincipal();
            return principal.getId();
        }
        throw new IllegalArgumentException("No se pudo obtener el ID del usuario del JWT");
    }
}