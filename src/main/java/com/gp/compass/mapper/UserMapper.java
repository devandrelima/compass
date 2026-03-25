package com.gp.compass.mapper;

import com.gp.compass.dto.UserResponse;
import com.gp.compass.entity.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
    }
}
