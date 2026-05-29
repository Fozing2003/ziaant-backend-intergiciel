package com.ziaant.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "token:";
    private static final long TTL_HOURS = 24;

    // Stocker le token après login
    public void storeToken(String token, String email) {
        redisTemplate.opsForValue().set(
            PREFIX + token, email, TTL_HOURS, TimeUnit.HOURS
        );
    }

    // Supprimer le token (logout ou suspension)
    public void revokeToken(String token) {
        redisTemplate.delete(PREFIX + token);
    }

    // Vérifier si le token est actif
    public boolean isTokenActive(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }

    // Révoquer tous les tokens d'un utilisateur par email
    public void revokeAllTokensForUser(String email) {
        var keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null) {
            keys.forEach(key -> {
                String storedEmail = redisTemplate.opsForValue().get(key);
                if (email.equals(storedEmail)) {
                    redisTemplate.delete(key);
                }
            });
        }
    }
}
