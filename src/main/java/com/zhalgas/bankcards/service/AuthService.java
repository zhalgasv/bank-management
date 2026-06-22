package com.zhalgas.bankcards.service;

import com.zhalgas.bankcards.dto.RegisterRequest;
import com.zhalgas.bankcards.entity.Role;
import com.zhalgas.bankcards.entity.User;
import com.zhalgas.bankcards.exception.UserAlreadyExistsException;
import com.zhalgas.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhalgas.bankcards.dto.AuthResponse;
import com.zhalgas.bankcards.dto.LoginRequest;
import com.zhalgas.bankcards.security.CustomUserDetailsService;
import com.zhalgas.bankcards.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtService jwtService
                       ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
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

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow();

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());

        String token = jwtService.generateToken(
                userDetails,
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole()
        );
    }
}
