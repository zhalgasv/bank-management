package com.zhalgas.bankcards.dto;

import com.zhalgas.bankcards.entity.Role;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        Role role
) {
}
