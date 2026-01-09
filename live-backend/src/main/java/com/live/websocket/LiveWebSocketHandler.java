package com.live.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket连接建立: {}", session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket连接关闭: {}, 状态: {}", session.getId(), status);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("收到消息: {}", message.getPayload());
    }
    
    public void broadcast(String type, Object data) {
        Map<String, Object> message = Map.of(
            "type", type,
            "data", data
        );
        
        try {
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);
            
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("发送消息失败: {}", e.getMessage());
                    }
                }
            });
            log.info("广播消息: type={}", type);
        } catch (Exception e) {
            log.error("序列化消息失败: {}", e.getMessage());
        }
    }
    
    public int getConnectionCount() {
        return sessions.size();
    }
}
