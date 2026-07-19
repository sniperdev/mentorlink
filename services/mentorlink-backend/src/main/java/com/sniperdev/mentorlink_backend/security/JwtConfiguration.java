package com.sniperdev.mentorlink_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class JwtConfiguration {

    private final JwtProperties jwtProperties;

    @Bean
    JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());

        SecretKey key = new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        return decoder;
    }
}
