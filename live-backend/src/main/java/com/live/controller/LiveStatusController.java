package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.entity.LiveStatus;
import com.live.entity.Stream;
import com.live.entity.VoteData;
import com.live.service.MockDataService;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LiveStatusController {

    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;

    @GetMapping("/api/admin/live/status")
    public ApiResponse<Map<String, Object>> getLiveStatus(@RequestParam(value = "stream_id", required = false) String streamId) {
        String effectiveStreamId = streamId != null ? streamId : mockDataService.getDefaultStreamId();
        Stream stream = mockDataService.getStream(effectiveStreamId).orElse(null);
        LiveStatus status = mockDataService.getLiveStatus(effectiveStreamId);

        Map<String, Object> data = new HashMap<>();
        data.put("streamId", effectiveStreamId);
        data.put("isLive", status != null && status.isLive());
        data.put("streamUrl", status != null ? status.getStreamUrl() : (stream != null ? stream.getUrl() : null));
        data.put("liveId", status != null ? status.getLiveId() : null);
        data.put("startTime", status != null ? status.getStartTime() : null);
        data.put("activeStreamUrl", stream != null ? stream.getUrl() : null);
        data.put("activeStreamId", stream != null ? stream.getId() : null);
        data.put("activeStreamName", stream != null ? stream.getName() : null);
        data.put("aiStatus", mockDataService.getAiStatus(effectiveStreamId));
        data.put("viewers", mockDataService.getViewers(effectiveStreamId));
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }

    @PostMapping("/api/live/control")
    public ApiResponse<Map<String, Object>> controlLive(@RequestBody Map<String, Object> body) {
        String action = body.get("action") != null ? body.get("action").toString() : null;
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : mockDataService.getDefaultStreamId();
        if (action == null || action.isEmpty()) {
            return ApiResponse.error("action 参数不能为空");
        }

        LiveStatus status;
        if ("start".equalsIgnoreCase(action)) {
            status = mockDataService.startLive(streamId);
            broadcastLiveStatus(streamId, true);
        } else if ("stop".equalsIgnoreCase(action)) {
            status = mockDataService.stopLive(streamId);
            broadcastLiveStatus(streamId, false);
        } else {
            return ApiResponse.error("action 必须是 start 或 stop");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("status", action.equalsIgnoreCase("start") ? "started" : "stopped");
        data.put("streamUrl", status != null ? status.getStreamUrl() : null);
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }

    @PostMapping("/api/v1/admin/live/start")
    public ApiResponse<Map<String, Object>> startLiveAdmin(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : null;
        if (streamId == null || streamId.isEmpty()) {
            return ApiResponse.error("streamId 不能为空");
        }
        LiveStatus status = mockDataService.startLive(streamId);
        broadcastLiveStatus(streamId, true);
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("status", "started");
        data.put("streamUrl", status != null ? status.getStreamUrl() : null);
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success("直播已开始", data);
    }

    @PostMapping("/api/v1/admin/live/stop")
    public ApiResponse<Map<String, Object>> stopLiveAdmin(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : null;
        if (streamId == null || streamId.isEmpty()) {
            return ApiResponse.error("streamId 不能为空");
        }
        mockDataService.stopLive(streamId);
        broadcastLiveStatus(streamId, false);
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("status", "stopped");
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success("直播已停止", data);
    }

    @PostMapping("/api/v1/admin/live/reset-votes")
    public ApiResponse<Map<String, Object>> resetVotes(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : mockDataService.getDefaultStreamId();
        Map<String, Object> resetTo = body.get("resetTo") instanceof Map ? (Map<String, Object>) body.get("resetTo") : new HashMap<>();
        Integer leftVotes = resetTo.get("leftVotes") != null ? Integer.parseInt(resetTo.get("leftVotes").toString()) : 0;
        Integer rightVotes = resetTo.get("rightVotes") != null ? Integer.parseInt(resetTo.get("rightVotes").toString()) : 0;
        VoteData voteData = mockDataService.updateVotes(streamId, leftVotes, rightVotes, "set");

        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("leftVotes", voteData.getLeftVotes());
        data.put("rightVotes", voteData.getRightVotes());
        data.put("totalVotes", voteData.getTotalVotes());
        data.put("timestamp", LocalDateTime.now().toString());
        webSocketHandler.broadcast("votes-updated", data);
        return ApiResponse.success("票数已重置", data);
    }

    @GetMapping("/api/v1/admin/live/viewers")
    public ApiResponse<Map<String, Object>> getViewers(@RequestParam(value = "stream_id", required = false) String streamId) {
        Map<String, Object> data = new HashMap<>();
        if (streamId != null && !streamId.isEmpty()) {
            int viewers = mockDataService.getViewers(streamId);
            data.put("streamId", streamId);
            data.put("viewers", viewers);
        } else {
            data.put("streams", mockDataService.getAllViewers());
            int total = mockDataService.getAllViewers().values().stream().mapToInt(Integer::intValue).sum();
            data.put("totalConnections", total);
        }
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }

    @PostMapping("/api/v1/admin/live/broadcast-viewers")
    public ApiResponse<Map<String, Object>> broadcastViewers(@RequestBody Map<String, Object> body) {
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : null;
        if (streamId == null || streamId.isEmpty()) {
            return ApiResponse.error("streamId 不能为空");
        }
        int viewers = mockDataService.bumpViewers(streamId);
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("viewers", viewers);
        data.put("timestamp", LocalDateTime.now().toString());
        webSocketHandler.broadcast("viewers-updated", data);
        return ApiResponse.success("观看人数已广播", data);
    }

    private void broadcastLiveStatus(String streamId, boolean isLive) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("streamId", streamId);
        payload.put("isLive", isLive);
        LiveStatus status = mockDataService.getLiveStatus(streamId);
        Stream stream = mockDataService.getStream(streamId).orElse(null);
        payload.put("streamUrl", status != null ? status.getStreamUrl() : (stream != null ? stream.getUrl() : null));
        payload.put("streamName", stream != null ? stream.getName() : null);
        payload.put("timestamp", LocalDateTime.now().toString());
        webSocketHandler.broadcast("liveStatus", payload);
    }
}
