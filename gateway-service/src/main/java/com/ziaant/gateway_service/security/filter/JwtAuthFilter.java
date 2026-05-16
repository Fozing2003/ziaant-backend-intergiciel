package com.ziaant.gateway_service.security.filter;

import com.ziaant.gateway_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
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

    // Routes accessibles SANS token
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/register/restaurateur",
            "/api/auth/register/admin",
            "/api/auth/login",
            "/api/auth/validate",
            "/api/restaurants",          // liste publique
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Si la route est publique, on laisse passer
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        // Vérification du header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Accès refusé sur {} : header Authorization manquant", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7).trim();

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Accès refusé sur {} : token JWT invalide ou expiré", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token valide — on ajoute les infos dans les headers pour les services en aval
        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                )
                .build();

        log.debug("Token valide — user: {}, role: {}, path: {}", email, role, path);
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1; // S'exécute en premier
    }
}
