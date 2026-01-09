package com.live.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveStatus {
    private String streamId;
    private boolean isLive;
    private String streamUrl;
    private String liveId;
    private long startTime;
}
