package com.example.chat.service.impl;

import com.example.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private final String NOT_FOUND_MESSAGE = "User with username %s not found";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(exceptionSupplier(username));
    }

    private Supplier<UsernameNotFoundException> exceptionSupplier(String username) {
        return () -> new UsernameNotFoundException(String.format(NOT_FOUND_MESSAGE, username));
    }
}
