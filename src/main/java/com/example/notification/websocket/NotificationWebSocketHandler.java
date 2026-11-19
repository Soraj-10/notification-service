package com.example.notification.websocket;

import com.example.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info(">>> WS connected: {}", session.getAttributes().get("username"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(Notification n) {
        String json = "{\"id\":" + n.getId()
                + ",\"status\":\"" + n.getStatus() + "\""
                + ",\"retryCount\":" + n.getRetryCount()
                + ",\"errorMessage\":" + quote(n.getErrorMessage()) + "}";

        for (WebSocketSession s : sessions) {
            if (!s.isOpen()) continue;
            boolean admin = Boolean.TRUE.equals(s.getAttributes().get("admin"));
            boolean owner = n.getCreatedBy() != null
                    && n.getCreatedBy().equals(s.getAttributes().get("username"));
            if (admin || owner) {
                try {
                    synchronized (s) {
                        s.sendMessage(new TextMessage(json));
                    }
                } catch (Exception e) {
                    log.warn(">>> WS send failed: {}", e.getMessage());
                }
            }
        }
    }

    private String quote(String text) {
        if (text == null) return "null";
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ") + "\"";
    }
}