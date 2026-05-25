package com.ziaant.gateway_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AccessTokenRevocationStore {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isRevoked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return Mono.just(true);
        }
        return redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId)
                .onErrorReturn(true);
    }
}
