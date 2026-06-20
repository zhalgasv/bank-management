package com.zhalgas.bankcards.service;

import com.zhalgas.bankcards.dto.RegisterRequest;
import com.zhalgas.bankcards.entity.Role;
import com.zhalgas.bankcards.entity.User;
import com.zhalgas.bankcards.exception.UserAlreadyExistsException;
import com.zhalgas.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Username already exists!"
            );
        }
        if(userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email already exists!"
            );
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.USER);

        userRepository.save(user);
    }
}
