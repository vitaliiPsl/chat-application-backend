package com.example.chat.utils;

import com.example.chat.model.user.User;
import com.example.chat.payload.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PayloadMapper {
    private final ModelMapper modelMapper;

    public UserDto mapUserToUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public User mapUserDtoToUser(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
