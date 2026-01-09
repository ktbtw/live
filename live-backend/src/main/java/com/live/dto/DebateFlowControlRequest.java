package com.live.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebateFlowControlRequest {
    private String streamId;
    private String action;
    private Integer segmentIndex;
}
