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
@RequiredArgsConstructor
public class StreamV1Controller {

    private final MockDataService mockDataService;

    @GetMapping("/api/v1/admin/streams")
    public ApiResponse<Map<String, Object>> getStreamsV1() {
        List<Stream> streams = mockDataService.getAllStreams();
        Map<String, Object> data = new HashMap<>();
        data.put("streams", streams);
        data.put("total", streams.size());
        return ApiResponse.success(data);
    }

    @PostMapping("/api/v1/admin/streams")
    public ApiResponse<Stream> createStream(@RequestBody Stream stream) {
        Stream created = mockDataService.createStream(stream);
        return ApiResponse.success("直播流已创建", created);
    }
}
