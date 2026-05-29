package com.ziaant.gateway_service.security.filter;
import com.ziaant.gateway_service.security.AccessTokenRevocationStore;
import com.ziaant.gateway_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtUtil jwtUtil;
    private final AccessTokenRevocationStore accessTokenRevocationStore;
    @Value("${security.internal-token}")
    private String internalToken;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/register/restaurateur",
            "/api/auth/register/admin",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/validate",
            "/api/restaurants",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Laisser passer les preflight CORS (OPTIONS)
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Access refused on {}: missing Authorization header", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Access refused on {}: invalid or expired JWT", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        String tokenId = jwtUtil.extractTokenId(token);
        return accessTokenRevocationStore.isRevoked(tokenId)
                .flatMap(revoked -> {
                    if (revoked) {
                        log.warn("Access refused on {}: revoked JWT", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    ServerWebExchange.Builder exchangeBuilder = exchange.mutate()
                            .request(r -> r
                                    .header("X-User-Email", email)
                                    .header("X-User-Role", role)
                            );
                    if (path.startsWith("/api/notifications")) {
                        exchangeBuilder.request(r -> r.header("X-Internal-Token", internalToken));
                    }
                    return chain.filter(exchangeBuilder.build());
                });
    }
    @Override
    public int getOrder() {
        return -1;
    }
}
