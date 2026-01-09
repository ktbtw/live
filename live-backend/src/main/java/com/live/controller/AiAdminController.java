package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.service.MockDataService;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiAdminController {

    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;

    @PostMapping("/api/v1/admin/ai/start")
    public ApiResponse<Map<String, Object>> startAi(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : mockDataService.getDefaultStreamId();
        String status = mockDataService.setAiStatus(streamId, "running");
        return broadcastAiStatus(streamId, status, "AI 已启动");
    }

    @PostMapping("/api/v1/admin/ai/stop")
    public ApiResponse<Map<String, Object>> stopAi(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : mockDataService.getDefaultStreamId();
        String status = mockDataService.setAiStatus(streamId, "stopped");
        return broadcastAiStatus(streamId, status, "AI 已停止");
    }

    @PostMapping("/api/v1/admin/ai/toggle")
    public ApiResponse<Map<String, Object>> toggleAi(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : mockDataService.getDefaultStreamId();
        String current = mockDataService.getAiStatus(streamId);
        String next = "running".equals(current) ? "stopped" : "running";
        String status = mockDataService.setAiStatus(streamId, next);
        return broadcastAiStatus(streamId, status, "AI 状态已切换");
    }

    private ApiResponse<Map<String, Object>> broadcastAiStatus(String streamId, String status, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("status", status);
        data.put("timestamp", LocalDateTime.now().toString());
        webSocketHandler.broadcast("aiStatus", data);
        return ApiResponse.success(message, data);
    }
}
