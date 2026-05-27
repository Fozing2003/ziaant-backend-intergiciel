package com.ziaant.gateway_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTokenRevocationStoreTest {

    private final ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
    private final AccessTokenRevocationStore store = new AccessTokenRevocationStore(redisTemplate);

    @Test
    void treatsMissingTokenIdAsRevoked() {
        assertThat(store.isRevoked("").block()).isTrue();
    }

    @Test
    void failsClosedWhenRedisIsUnavailable() {
        when(redisTemplate.hasKey("auth:blacklist:access:jti-1"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        assertThat(store.isRevoked("jti-1").block()).isTrue();
    }
}
