package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.entity.Stream;
import com.live.service.MockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class StreamController {
    
    private final MockDataService mockDataService;
    
    @GetMapping("/streams")
    public List<Stream> getStreams() {
        return mockDataService.getAllStreams();
    }
    
    @GetMapping("/streams/{id}")
    public ApiResponse<Stream> getStream(@PathVariable String id) {
        return mockDataService.getStream(id)
            .map(ApiResponse::success)
            .orElse(ApiResponse.error("直播流不存在"));
    }

    @PostMapping("/streams")
    public ApiResponse<Stream> createStreamLegacy(@RequestBody Stream stream) {
        Stream created = mockDataService.createStream(stream);
        return ApiResponse.success("直播流已创建", created);
    }

    @PutMapping("/streams/{id}")
    public ApiResponse<Stream> updateStream(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Stream existing = mockDataService.getStream(id).orElse(null);
        if (existing == null) {
            return ApiResponse.error("直播流不存在");
        }

        Stream update = new Stream();
        if (body.containsKey("name")) {
            update.setName(body.get("name") != null ? body.get("name").toString() : null);
        }
        if (body.containsKey("url")) {
            update.setUrl(body.get("url") != null ? body.get("url").toString() : null);
        }
        if (body.containsKey("type")) {
            update.setType(body.get("type") != null ? body.get("type").toString() : null);
        }
        if (body.containsKey("description")) {
            update.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        }
        if (body.containsKey("enabled")) {
            update.setEnabled(Boolean.parseBoolean(body.get("enabled").toString()));
        } else {
            update.setEnabled(existing.isEnabled());
        }

        Stream updated = mockDataService.updateStream(id, update);
        return ApiResponse.success("直播流已更新", updated);
    }

    @DeleteMapping("/streams/{id}")
    public ApiResponse<Map<String, Object>> deleteStream(@PathVariable String id) {
        boolean deleted = mockDataService.deleteStream(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("deleted", deleted);
        return deleted ? ApiResponse.success("直播流已删除", data) : ApiResponse.error("直播流不存在");
    }

    @PostMapping("/streams/{id}/toggle")
    public ApiResponse<Stream> toggleStream(@PathVariable String id) {
        Stream updated = mockDataService.toggleStream(id);
        if (updated == null) {
            return ApiResponse.error("直播流不存在");
        }
        return ApiResponse.success("直播流状态已切换", updated);
    }
}
