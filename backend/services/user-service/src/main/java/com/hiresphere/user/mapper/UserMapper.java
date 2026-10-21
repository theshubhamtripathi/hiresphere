package com.hiresphere.user.mapper;

import com.hiresphere.user.domain.User;
import com.hiresphere.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
