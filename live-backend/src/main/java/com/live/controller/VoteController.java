package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.dto.UpdateVotesRequest;
import com.live.dto.UserVoteRequest;
import com.live.entity.LiveStatus;
import com.live.entity.Stream;
import com.live.entity.VoteData;
import com.live.entity.UserVoteRecord;
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
@RequiredArgsConstructor
public class VoteController {
    
    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;
    
    @GetMapping("/api/admin/votes")
    public ApiResponse<VoteData> getVotes(@RequestParam(value = "stream_id", defaultValue = "stream-1") String streamId) {
        return ApiResponse.success(mockDataService.getVotes(streamId));
    }
    
    @GetMapping("/api/admin/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(@RequestParam(value = "stream_id", required = false) String streamId) {
        String effectiveStreamId = streamId != null ? streamId : mockDataService.getDefaultStreamId();
        VoteData voteData = mockDataService.getVotes(effectiveStreamId);
        Stream stream = mockDataService.getStream(effectiveStreamId).orElse(null);
        LiveStatus liveStatus = mockDataService.getLiveStatus(effectiveStreamId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("streamId", effectiveStreamId);
        dashboard.put("votes", voteData);
        dashboard.put("leftVotes", voteData.getLeftVotes());
        dashboard.put("rightVotes", voteData.getRightVotes());
        dashboard.put("totalVotes", voteData.getTotalVotes());
        dashboard.put("leftPercentage", voteData.getLeftPercentage());
        dashboard.put("rightPercentage", voteData.getRightPercentage());
        dashboard.put("isLive", liveStatus != null && liveStatus.isLive());
        dashboard.put("liveStreamUrl", liveStatus != null ? liveStatus.getStreamUrl() : (stream != null ? stream.getUrl() : null));
        dashboard.put("activeStreamUrl", stream != null ? stream.getUrl() : null);
        dashboard.put("activeStreamId", stream != null ? stream.getId() : null);
        dashboard.put("activeStreamName", stream != null ? stream.getName() : null);
        dashboard.put("activeUsers", mockDataService.getViewers(effectiveStreamId));
        dashboard.put("totalUsers", mockDataService.getAllUsers().size());
        dashboard.put("aiStatus", mockDataService.getAiStatus(effectiveStreamId));
        dashboard.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(dashboard);
    }

    @GetMapping("/api/v1/admin/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardV1(@RequestParam(value = "stream_id", required = false) String streamId) {
        return getDashboard(streamId);
    }
    
    @PutMapping("/api/admin/votes")
    public ApiResponse<VoteData> setVotes(@RequestBody UpdateVotesRequest request) {
        String streamId = request.getStreamId() != null ? request.getStreamId() : "stream-1";
        VoteData voteData = mockDataService.updateVotes(streamId, request.getLeftVotes(), request.getRightVotes(), "set");
        broadcastVotesUpdate(voteData);
        return ApiResponse.success("票数设置成功", voteData);
    }
    
    @PostMapping("/api/admin/live/update-votes")
    public ApiResponse<VoteData> updateVotes(@RequestBody UpdateVotesRequest request) {
        String streamId = request.getStreamId() != null ? request.getStreamId() : "stream-1";
        String action = request.getAction() != null ? request.getAction() : "set";
        VoteData voteData = mockDataService.updateVotes(streamId, request.getLeftVotes(), request.getRightVotes(), action);
        broadcastVotesUpdate(voteData);
        return ApiResponse.success("票数更新成功", voteData);
    }
    
    @PostMapping("/api/user-vote")
    public ApiResponse<VoteData> userVote(@RequestBody UserVoteRequest request) {
        UserVoteRequest effectiveRequest = request.getEffectiveRequest();
        String streamId = effectiveRequest.getStreamId() != null ? effectiveRequest.getStreamId() : "stream-1";
        String userId = effectiveRequest.getUserId() != null ? effectiveRequest.getUserId() : "anonymous";
        
        VoteData voteData = mockDataService.addUserVote(streamId, effectiveRequest.getLeftVotes(), effectiveRequest.getRightVotes());
        mockDataService.recordUserVote(streamId, userId,
            effectiveRequest.getLeftVotes() != null ? effectiveRequest.getLeftVotes() : 0,
            effectiveRequest.getRightVotes() != null ? effectiveRequest.getRightVotes() : 0);
        
        Map<String, Object> broadcastData = new HashMap<>();
        broadcastData.put("leftVotes", voteData.getLeftVotes());
        broadcastData.put("rightVotes", voteData.getRightVotes());
        broadcastData.put("totalVotes", voteData.getTotalVotes());
        broadcastData.put("leftPercentage", voteData.getLeftPercentage());
        broadcastData.put("rightPercentage", voteData.getRightPercentage());
        broadcastData.put("streamId", streamId);
        broadcastData.put("userVote", Map.of(
            "userId", userId,
            "leftVotes", effectiveRequest.getLeftVotes() != null ? effectiveRequest.getLeftVotes() : 0,
            "rightVotes", effectiveRequest.getRightVotes() != null ? effectiveRequest.getRightVotes() : 0,
            "mode", effectiveRequest.getMode() != null ? effectiveRequest.getMode() : "100票分配制"
        ));
        broadcastData.put("timestamp", LocalDateTime.now().toString());
        
        webSocketHandler.broadcast("votes-updated", broadcastData);
        log.info("用户投票: streamId={}, left={}, right={}", streamId, effectiveRequest.getLeftVotes(), effectiveRequest.getRightVotes());
        
        return ApiResponse.success("投票成功", voteData);
    }
    
    @PostMapping("/api/v1/user-vote")
    public ApiResponse<VoteData> userVoteV1(@RequestBody UserVoteRequest request) {
        return userVote(request);
    }
    
    @PostMapping("/api/v1/admin/live/update-votes")
    public ApiResponse<VoteData> updateVotesV1(@RequestBody UpdateVotesRequest request) {
        return updateVotes(request);
    }

    @GetMapping("/api/v1/votes")
    public VoteData getVotesV1(@RequestParam(value = "stream_id") String streamId) {
        return mockDataService.getVotes(streamId);
    }

    @GetMapping("/api/votes")
    public VoteData getVotesLegacy(@RequestParam(value = "stream_id", required = false) String streamId) {
        String effectiveStreamId = streamId != null ? streamId : mockDataService.getDefaultStreamId();
        return mockDataService.getVotes(effectiveStreamId);
    }

    @GetMapping("/api/admin/votes/statistics")
    public ApiResponse<Map<String, Object>> getVotesStatistics(@RequestParam(value = "stream_id", required = false) String streamId,
                                                               @RequestParam(value = "timeRange", required = false) String timeRange) {
        String effectiveStreamId = streamId != null ? streamId : mockDataService.getDefaultStreamId();
        VoteData voteData = mockDataService.getVotes(effectiveStreamId);
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", effectiveStreamId);
        data.put("timeRange", timeRange != null ? timeRange : "1h");
        data.put("leftVotes", voteData.getLeftVotes());
        data.put("rightVotes", voteData.getRightVotes());
        data.put("totalVotes", voteData.getTotalVotes());
        data.put("leftPercentage", voteData.getLeftPercentage());
        data.put("rightPercentage", voteData.getRightPercentage());
        data.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }

    @GetMapping("/api/v1/admin/votes/statistics")
    public ApiResponse<Map<String, Object>> getVotesStatisticsV1(@RequestParam(value = "stream_id", required = false) String streamId,
                                                                 @RequestParam(value = "timeRange", required = false) String timeRange) {
        return getVotesStatistics(streamId, timeRange);
    }

    @GetMapping("/api/v1/user-votes")
    public ApiResponse<Map<String, Object>> getUserVotes(@RequestParam(value = "stream_id") String streamId,
                                                         @RequestParam(value = "user_id") String userId) {
        UserVoteRecord record = mockDataService.getUserVote(streamId, userId);
        Map<String, Object> data = new HashMap<>();
        if (record == null) {
            data.put("streamId", streamId);
            data.put("userId", userId);
            data.put("leftVotes", 0);
            data.put("rightVotes", 0);
        } else {
            data.put("streamId", record.getStreamId());
            data.put("userId", record.getUserId());
            data.put("leftVotes", record.getLeftVotes());
            data.put("rightVotes", record.getRightVotes());
        }
        return ApiResponse.success(data);
    }
    
    private void broadcastVotesUpdate(VoteData voteData) {
        Map<String, Object> broadcastData = new HashMap<>();
        broadcastData.put("leftVotes", voteData.getLeftVotes());
        broadcastData.put("rightVotes", voteData.getRightVotes());
        broadcastData.put("totalVotes", voteData.getTotalVotes());
        broadcastData.put("leftPercentage", voteData.getLeftPercentage());
        broadcastData.put("rightPercentage", voteData.getRightPercentage());
        broadcastData.put("streamId", voteData.getStreamId());
        broadcastData.put("timestamp", LocalDateTime.now().toString());
        
        webSocketHandler.broadcast("votes-updated", broadcastData);
    }
}
