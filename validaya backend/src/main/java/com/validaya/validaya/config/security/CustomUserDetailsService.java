package com.validaya.validaya.config.security;

import com.validaya.validaya.entity.User;
import com.validaya.validaya.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Intentar buscar por identificación primero (nuevo flujo)
        User user = userRepository.findByIdentification(username).orElse(null);
        
        // Si no existe por identificación, buscar por email (compatibilidad)
        if (user == null) {
            user = userRepository.findByEmail(username).orElse(null);
        }
        
        if (user == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        
        return UserPrincipal.from(user);
    }
}