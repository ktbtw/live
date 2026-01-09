package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.dto.DebateFlowConfig;
import com.live.dto.DebateFlowControlRequest;
import com.live.entity.DebateSegment;
import com.live.service.MockDataService;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DebateFlowController {
    
    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;
    
    @GetMapping("/debate-flow")
    public ApiResponse<DebateFlowConfig> getDebateFlow(@RequestParam(value = "stream_id", defaultValue = "stream-1") String streamId) {
        return ApiResponse.success(mockDataService.getDebateFlow(streamId));
    }
    
    @PostMapping("/debate-flow")
    public ApiResponse<DebateFlowConfig> saveDebateFlow(@RequestBody DebateFlowConfig request) {
        String streamId = request.getStreamId() != null ? request.getStreamId() : "stream-1";
        DebateFlowConfig config = mockDataService.saveDebateFlow(streamId, request.getFlow());
        
        Map<String, Object> broadcastData = new HashMap<>();
        broadcastData.put("streamId", streamId);
        broadcastData.put("flow", config.getFlow());
        broadcastData.put("timestamp", LocalDateTime.now().toString());
        
        webSocketHandler.broadcast("debate-flow-updated", broadcastData);
        log.info("辩论流程配置已更新: streamId={}", streamId);
        
        return ApiResponse.success("辩论流程配置保存成功", config);
    }
    
    @PostMapping("/debate-flow/control")
    public ApiResponse<Map<String, Object>> controlDebateFlow(@RequestBody DebateFlowControlRequest request) {
        String streamId = request.getStreamId() != null ? request.getStreamId() : "stream-1";
        String action = request.getAction();
        
        if (action == null || action.isEmpty()) {
            return ApiResponse.error("action参数不能为空");
        }
        
        Map<String, Object> broadcastData = new HashMap<>();
        broadcastData.put("streamId", streamId);
        broadcastData.put("action", action);
        if (request.getSegmentIndex() != null) {
            broadcastData.put("segmentIndex", request.getSegmentIndex());
        }
        broadcastData.put("timestamp", LocalDateTime.now().toString());
        
        webSocketHandler.broadcast("debate-flow-control", broadcastData);
        log.info("辩论流程控制命令: streamId={}, action={}", streamId, action);
        
        Map<String, Object> result = new HashMap<>();
        result.put("streamId", streamId);
        result.put("action", action);
        result.put("executed", true);
        
        return ApiResponse.success("控制命令已发送", result);
    }
}
