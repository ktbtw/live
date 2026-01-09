package com.live.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiComment {
    private String id;
    private String contentId;
    private String user;
    private String avatar;
    private String text;
    private int likes;
    private long timestamp;
}
