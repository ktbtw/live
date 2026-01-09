package com.live.controller;

import com.live.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WechatController {

    @PostMapping("/api/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> body) {
        String code = body.get("code") != null ? body.get("code").toString() : null;
        Map<String, Object> userInfo = body.get("userInfo") instanceof Map ? (Map<String, Object>) body.get("userInfo") : null;

        if (code == null || code.isEmpty()) {
            return ApiResponse.error("code 不能为空");
        }

        String userId = UUID.randomUUID().toString();
        String nickname = userInfo != null && userInfo.get("nickName") != null ? userInfo.get("nickName").toString() : "微信用户";
        String avatar = userInfo != null && userInfo.get("avatarUrl") != null ? userInfo.get("avatarUrl").toString() : "/static/logo.png";

        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("nickname", nickname);
        user.put("avatar", avatar);

        Map<String, Object> data = new HashMap<>();
        data.put("token", UUID.randomUUID().toString().replace("-", ""));
        data.put("user", user);
        data.put("openid", "mock_openid_" + userId.substring(0, 8));
        return ApiResponse.success("登录成功", data);
    }
}
