package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.entity.Debate;
import com.live.service.MockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DebateController {

    private final MockDataService mockDataService;

    @GetMapping("/api/v1/debate-topic")
    public ApiResponse<Debate> getDebateTopic(@RequestParam(value = "stream_id", required = false) String streamId) {
        Debate debate = mockDataService.getDebateByStream(streamId);
        if (debate == null) {
            return ApiResponse.error("辩题不存在");
        }
        return ApiResponse.success(debate);
    }

    @GetMapping("/api/v1/admin/streams/{streamId}/debate")
    public ApiResponse<Debate> getStreamDebate(@PathVariable String streamId) {
        Debate debate = mockDataService.getDebateByStream(streamId);
        return ApiResponse.success(debate);
    }

    @PutMapping("/api/v1/admin/streams/{streamId}/debate")
    public ApiResponse<Debate> associateDebate(@PathVariable String streamId, @RequestBody Map<String, Object> body) {
        String debateId = body.get("debate_id") != null ? body.get("debate_id").toString() : null;
        Debate debate = mockDataService.associateDebate(streamId, debateId);
        if (debate == null) {
            return ApiResponse.error("辩题不存在");
        }
        return ApiResponse.success("辩题已关联", debate);
    }

    @DeleteMapping("/api/v1/admin/streams/{streamId}/debate")
    public ApiResponse<Map<String, Object>> deleteStreamDebate(@PathVariable String streamId) {
        mockDataService.clearDebateAssociation(streamId);
        Map<String, Object> data = new HashMap<>();
        data.put("streamId", streamId);
        data.put("deleted", true);
        return ApiResponse.success("辩题关联已删除", data);
    }

    @PostMapping("/api/v1/admin/debates")
    public ApiResponse<Debate> createDebate(@RequestBody Debate debate) {
        Debate created = mockDataService.createDebate(debate);
        return ApiResponse.success("辩题已创建", created);
    }

    @GetMapping("/api/v1/admin/debates/{debateId}")
    public ApiResponse<Debate> getDebate(@PathVariable String debateId) {
        Debate debate = mockDataService.getDebateById(debateId);
        if (debate == null) {
            return ApiResponse.error("辩题不存在");
        }
        return ApiResponse.success(debate);
    }

    @PutMapping("/api/v1/admin/debates/{debateId}")
    public ApiResponse<Debate> updateDebate(@PathVariable String debateId, @RequestBody Debate debate) {
        Debate updated = mockDataService.updateDebate(debateId, debate);
        if (updated == null) {
            return ApiResponse.error("辩题不存在");
        }
        return ApiResponse.success("辩题已更新", updated);
    }
}
