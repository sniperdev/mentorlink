package com.sniperdev.auth_service.auth.refresh.service;

import com.sniperdev.auth_service.auth.jwt.JwtProperties;
import com.sniperdev.auth_service.auth.refresh.model.RefreshToken;
import com.sniperdev.auth_service.auth.refresh.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(UUID userId) {

        String refreshToken = refreshTokenGenerator.generate();

        String tokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(
                        Instant.now()
                                .plusMillis(jwtProperties.refreshTokenExpiration())
                )
                .build();

        refreshTokenRepository.save(entity);

        return refreshToken;
    }
}
