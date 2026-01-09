package com.live.controller;

import com.live.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RtmpController {

    @GetMapping("/api/admin/rtmp/urls")
    public ApiResponse<Map<String, Object>> getRtmpUrls(@RequestParam(value = "room_name") String roomName) {
        String serverHost = "127.0.0.1";
        String pushUrl = String.format("rtmp://%s/live/%s", serverHost, roomName);
        String playFlv = String.format("http://%s:8088/live/%s.flv", serverHost, roomName);
        String playHls = String.format("http://%s:8088/live/%s.m3u8", serverHost, roomName);

        Map<String, Object> data = new HashMap<>();
        data.put("room_name", roomName);
        data.put("push_url", pushUrl);
        data.put("play_flv", playFlv);
        data.put("play_hls", playHls);
        return ApiResponse.success(data);
    }
}
