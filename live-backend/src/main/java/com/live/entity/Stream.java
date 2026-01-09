package com.live.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream {
    private String id;
    private String name;
    private String url;
    private String type;
    private boolean enabled;
    private String description;
    private LiveStatus liveStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
