package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.dto.JudgesConfig;
import com.live.entity.Judge;
import com.live.service.MockDataService;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class JudgeController {
    
    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;
    
    @GetMapping("/judges")
    public ApiResponse<JudgesConfig> getJudges(@RequestParam(value = "stream_id", defaultValue = "stream-1") String streamId) {
        return ApiResponse.success(mockDataService.getJudges(streamId));
    }
    
    @PostMapping("/judges")
    public ApiResponse<JudgesConfig> saveJudges(@RequestBody JudgesConfig request) {
        String streamId = request.getStreamId() != null ? request.getStreamId() : "stream-1";
        JudgesConfig config = mockDataService.saveJudges(streamId, request.getJudges());
        
        Map<String, Object> broadcastData = new HashMap<>();
        broadcastData.put("streamId", streamId);
        broadcastData.put("judges", config.getJudges());
        broadcastData.put("timestamp", LocalDateTime.now().toString());
        
        webSocketHandler.broadcast("judges-updated", broadcastData);
        log.info("评委配置已更新: streamId={}", streamId);
        
        return ApiResponse.success("评委配置保存成功", config);
    }
}
