package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.dto.UserDto;
import com.validaya.validaya.repository.UserRepository;
import com.validaya.validaya.service.UserService;
import com.validaya.validaya.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Autowired(required = false)
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto.Response getById(Long id) {
        User user = findOrThrow(id);
        return MapperUtil.toUserResponse(user);
    }

    @Override
    public UserDto.Response getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));
        return MapperUtil.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserDto.Response update(Long id, UserDto.UpdateRequest request) {
        User user = findOrThrow(id);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        return MapperUtil.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long id, UserDto.ChangePasswordRequest request) {
        User user = findOrThrow(id);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        User user = findOrThrow(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public List<UserDto.Response> findAll() {
        return userRepository.findAll().stream()
                .map(MapperUtil::toUserResponse)
                .collect(Collectors.toList());
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }
}