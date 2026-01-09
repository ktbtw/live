package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {
    
    private final LiveWebSocketHandler webSocketHandler;
    
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now().toString());
        data.put("wsConnections", webSocketHandler.getConnectionCount());
        return ApiResponse.success(data);
    }
    
    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> apiHealth() {
        return health();
    }
}
