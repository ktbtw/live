package com.live.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebateSegment {
    private String name;
    private Integer duration;
    private String side;
    private Integer order;
}
