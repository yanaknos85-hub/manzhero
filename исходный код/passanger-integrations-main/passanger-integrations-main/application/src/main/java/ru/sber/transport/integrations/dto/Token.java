package ru.sber.transport.integrations.dto;

import java.time.Instant;

public record Token(
        String token,
        Instant expirationToken,
        String refreshToken,
        Instant expirationRefreshToken,
        Boolean transferPassword
) {

}

