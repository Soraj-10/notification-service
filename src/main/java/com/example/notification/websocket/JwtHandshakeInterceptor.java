package com.example.notification.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder decoder;

    public JwtHandshakeInterceptor(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = UriComponentsBuilder.fromUri(request.getURI())
                    .build().getQueryParams().getFirst("token");
            if (token == null) return false;

            Jwt jwt = decoder.decode(token);
            List<String> roles = jwt.getClaimAsStringList("roles");
            attributes.put("username", jwt.getSubject());
            attributes.put("admin", roles != null && roles.contains("ADMIN"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}