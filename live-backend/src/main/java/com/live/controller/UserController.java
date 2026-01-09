package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.entity.User;
import com.live.service.MockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {
    
    private final MockDataService mockDataService;
    
    @GetMapping("/api/admin/users")
    public ApiResponse<Map<String, Object>> getUsers() {
        List<User> users = mockDataService.getAllUsers();
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("total", users.size());
        return ApiResponse.success(data);
    }

    @GetMapping("/api/v1/admin/users")
    public ApiResponse<Map<String, Object>> getUsersV1() {
        return getUsers();
    }
    
    @GetMapping("/api/admin/users/{id}")
    public ApiResponse<User> getUser(@PathVariable String id) {
        return mockDataService.getUser(id)
            .map(ApiResponse::success)
            .orElse(ApiResponse.error("用户不存在"));
    }

    @GetMapping("/api/admin/miniprogram/users")
    public ApiResponse<Map<String, Object>> getMiniProgramUsers(@RequestParam(value = "page", defaultValue = "1") int page,
                                                                @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                                                @RequestParam Map<String, String> filters) {
        List<User> users = mockDataService.getAllUsers();
        int total = users.size();
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(total, fromIndex + pageSize);
        List<User> items = fromIndex < toIndex ? users.subList(fromIndex, toIndex) : List.of();

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        data.put("items", items);
        return ApiResponse.success(data);
    }
}
