package com.validaya.validaya.service;

import com.validaya.validaya.entity.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto.Response getById(Long id);

    UserDto.Response getByEmail(String email);

    UserDto.Response update(Long id, UserDto.UpdateRequest request);

    void changePassword(Long id, UserDto.ChangePasswordRequest request);

    void deactivate(Long id);

    List<UserDto.Response> findAll();
}