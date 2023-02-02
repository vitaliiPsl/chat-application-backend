package com.example.chat.service;

import com.example.chat.model.user.User;
import com.example.chat.payload.user.UserDto;

import java.util.List;

/**
 * User service
 *
 * @see com.example.chat.model.user.User
 */
public interface UserService {

    /**
     * Get user as domain object
     *
     * @param userId id of the user
     * @return retrieved user
     */
    User getUserDomainObject(String userId);

    /**
     * Get user by the give id
     *
     * @param userId id of the user
     * @return retrieved user
     */
    UserDto getUser(String userId);

    /**
     * Get users by the given nickname
     *
     * @param nickname nickname of the user
     * @return list of users with nickname that matches given one
     */
    List<UserDto> getUsersByNickname(String nickname);
}
