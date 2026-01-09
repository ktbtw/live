package com.live.controller;

import com.live.dto.ApiResponse;
import com.live.entity.AiComment;
import com.live.entity.AiContent;
import com.live.service.MockDataService;
import com.live.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AiContentController {

    private final MockDataService mockDataService;
    private final LiveWebSocketHandler webSocketHandler;

    @GetMapping("/api/v1/ai-content")
    public ApiResponse<List<Map<String, Object>>> getAiContent(@RequestParam(value = "stream_id", required = false) String streamId) {
        List<AiContent> contents = mockDataService.getAiContents(streamId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (AiContent content : contents) {
            items.add(convertToClientContent(content));
        }
        return ApiResponse.success(items);
    }

    @PostMapping("/api/comment")
    public ApiResponse<Map<String, Object>> addComment(@RequestBody Map<String, Object> body) {
        String contentId = body.get("contentId") != null ? body.get("contentId").toString() : null;
        String text = body.get("text") != null ? body.get("text").toString() : null;
        String user = body.get("user") != null ? body.get("user").toString() : "匿名用户";
        String avatar = body.get("avatar") != null ? body.get("avatar").toString() : "👤";

        if (contentId == null || text == null || text.isEmpty()) {
            return ApiResponse.error("contentId 和 text 不能为空");
        }

        AiComment comment = mockDataService.addComment(contentId, text, user, avatar);
        if (comment == null) {
            return ApiResponse.error("内容不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getId());
        data.put("contentId", contentId);
        data.put("user", comment.getUser());
        data.put("avatar", comment.getAvatar());
        data.put("text", comment.getText());
        data.put("likes", comment.getLikes());
        data.put("timestamp", comment.getTimestamp());

        webSocketHandler.broadcast("comment-added", data);
        return ApiResponse.success("评论已添加", data);
    }

    @DeleteMapping("/api/comment/{commentId}")
    public ApiResponse<Map<String, Object>> deleteComment(@PathVariable String commentId, @RequestBody Map<String, Object> body) {
        String contentId = body.get("contentId") != null ? body.get("contentId").toString() : null;
        if (contentId == null) {
            return ApiResponse.error("contentId 不能为空");
        }

        AiComment deleted = mockDataService.deleteComment(contentId, commentId);
        if (deleted == null) {
            return ApiResponse.error("评论不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("commentId", commentId);
        data.put("contentId", contentId);
        webSocketHandler.broadcast("comment-deleted", data);
        return ApiResponse.success("评论已删除", data);
    }

    @PostMapping("/api/like")
    public ApiResponse<Map<String, Object>> like(@RequestBody Map<String, Object> body) {
        String contentId = body.get("contentId") != null ? body.get("contentId").toString() : null;
        String commentId = body.get("commentId") != null ? body.get("commentId").toString() : null;
        if (contentId == null) {
            return ApiResponse.error("contentId 不能为空");
        }

        AiContent content = mockDataService.likeContent(contentId, commentId);
        if (content == null) {
            return ApiResponse.error("内容不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("commentId", commentId);
        data.put("likes", content.getLikes());
        return ApiResponse.success("点赞成功", data);
    }

    @PostMapping("/api/admin/ai-content")
    public ApiResponse<Map<String, Object>> createAiContent(@RequestBody Map<String, Object> body) {
        String text = body.get("text") != null ? body.get("text").toString() : null;
        String side = body.get("side") != null ? body.get("side").toString() : "left";
        String debateId = body.get("debate_id") != null ? body.get("debate_id").toString() : null;
        String streamId = body.get("streamId") != null ? body.get("streamId").toString() : null;

        if (text == null || text.isEmpty()) {
            return ApiResponse.error("text 不能为空");
        }

        AiContent created = mockDataService.addAiContent(streamId, AiContent.builder()
            .text(text)
            .side(side)
            .debateId(debateId)
            .build());

        Map<String, Object> data = convertToClientContent(created);
        webSocketHandler.broadcast("newAIContent", data);
        return ApiResponse.success("内容已创建", data);
    }

    @PutMapping("/api/admin/ai-content/{contentId}")
    public ApiResponse<Map<String, Object>> updateAiContent(@PathVariable String contentId, @RequestBody Map<String, Object> body) {
        String text = body.get("text") != null ? body.get("text").toString() : null;
        String side = body.get("side") != null ? body.get("side").toString() : null;
        String debateId = body.get("debate_id") != null ? body.get("debate_id").toString() : null;

        AiContent updated = mockDataService.updateAiContent(contentId, AiContent.builder()
            .text(text)
            .side(side)
            .debateId(debateId)
            .build());

        if (updated == null) {
            return ApiResponse.error("内容不存在");
        }

        Map<String, Object> data = convertToClientContent(updated);
        webSocketHandler.broadcast("ai-content-updated", data);
        return ApiResponse.success("内容已更新", data);
    }

    @DeleteMapping({"/api/admin/ai/content/{contentId}", "/api/admin/ai-content/{contentId}"})
    public ApiResponse<Map<String, Object>> deleteAiContent(@PathVariable String contentId, @RequestBody(required = false) Map<String, Object> body) {
        boolean removed = mockDataService.deleteAiContent(contentId);
        if (!removed) {
            return ApiResponse.error("内容不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("reason", body != null ? body.getOrDefault("reason", "").toString() : "");
        webSocketHandler.broadcast("aiContentDeleted", data);
        return ApiResponse.success("内容已删除", data);
    }

    @GetMapping("/api/v1/admin/ai-content/list")
    public ApiResponse<Map<String, Object>> getAiContentList(@RequestParam(value = "page", defaultValue = "1") int page,
                                                             @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                                             @RequestParam(value = "startTime", required = false) String startTime,
                                                             @RequestParam(value = "endTime", required = false) String endTime,
                                                             @RequestParam(value = "stream_id", required = false) String streamId) {
        List<AiContent> contents = mockDataService.getAiContents(streamId);
        List<AiContent> filtered = new ArrayList<>(contents);

        if (startTime != null && !startTime.isEmpty()) {
            long start = parseIso(startTime);
            filtered.removeIf(item -> item.getTimestamp() < start);
        }
        if (endTime != null && !endTime.isEmpty()) {
            long end = parseIso(endTime);
            filtered.removeIf(item -> item.getTimestamp() > end);
        }

        int total = filtered.size();
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(total, fromIndex + pageSize);
        List<AiContent> pageItems = fromIndex < toIndex ? filtered.subList(fromIndex, toIndex) : new ArrayList<>();

        List<Map<String, Object>> items = new ArrayList<>();
        for (AiContent content : pageItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", content.getId());
            item.put("content", content.getText());
            item.put("text", content.getText());
            item.put("type", "summary");
            item.put("timestamp", Instant.ofEpochMilli(content.getTimestamp()).toString());
            item.put("position", "right".equalsIgnoreCase(content.getSide()) ? "right" : "left");
            item.put("confidence", 0.95);
            Map<String, Object> stats = new HashMap<>();
            stats.put("views", 0);
            stats.put("likes", content.getLikes());
            stats.put("comments", content.getComments() != null ? content.getComments().size() : 0);
            item.put("statistics", stats);
            items.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("page", page);
        data.put("items", items);
        return ApiResponse.success(data);
    }

    @GetMapping("/api/v1/admin/ai-content/{contentId}/comments")
    public ApiResponse<Map<String, Object>> getAiContentComments(@PathVariable String contentId,
                                                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                                                 @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        AiContent content = mockDataService.getAiContentById(contentId);
        if (content == null) {
            return ApiResponse.error("内容不存在");
        }

        List<AiComment> comments = content.getComments() != null ? content.getComments() : new ArrayList<>();
        int total = comments.size();
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(total, fromIndex + pageSize);
        List<AiComment> pageItems = fromIndex < toIndex ? comments.subList(fromIndex, toIndex) : new ArrayList<>();

        List<Map<String, Object>> formatted = new ArrayList<>();
        for (AiComment comment : pageItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("commentId", comment.getId());
            item.put("userId", comment.getUser());
            item.put("nickname", comment.getUser());
            item.put("avatar", comment.getAvatar());
            item.put("content", comment.getText());
            item.put("likes", comment.getLikes());
            item.put("timestamp", Instant.ofEpochMilli(comment.getTimestamp()).toString());
            formatted.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("contentId", contentId);
        data.put("contentText", content.getText());
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("comments", formatted);
        return ApiResponse.success(data);
    }

    @DeleteMapping("/api/v1/admin/ai-content/{contentId}/comments/{commentId}")
    public ApiResponse<Map<String, Object>> deleteAiContentComment(@PathVariable String contentId,
                                                                   @PathVariable String commentId,
                                                                   @RequestBody(required = false) Map<String, Object> body) {
        AiComment deleted = mockDataService.deleteComment(contentId, commentId);
        if (deleted == null) {
            return ApiResponse.error("评论不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", commentId);
        data.put("contentId", contentId);
        data.put("deleteTime", null);
        return ApiResponse.success("评论已删除", data);
    }

    private Map<String, Object> convertToClientContent(AiContent content) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", content.getId());
        item.put("debate_id", content.getDebateId());
        item.put("text", content.getText());
        item.put("side", content.getSide());
        item.put("timestamp", content.getTimestamp());
        item.put("comments", content.getComments());
        item.put("likes", content.getLikes());
        return item;
    }

    private long parseIso(String value) {
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (Exception ex) {
            try {
                return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception ignored) {
                return 0L;
            }
        }
    }
}
