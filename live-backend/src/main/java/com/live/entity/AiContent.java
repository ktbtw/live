package com.live.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiContent {
    private String id;
    private String debateId;
    private String streamId;
    private String text;
    private String side;
    private long timestamp;
    @Builder.Default
    private List<AiComment> comments = new ArrayList<>();
    private int likes;
}
