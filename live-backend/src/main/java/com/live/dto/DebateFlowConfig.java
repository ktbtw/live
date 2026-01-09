package com.live.dto;

import com.live.entity.DebateSegment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebateFlowConfig {
    private String streamId;
    private List<DebateSegment> flow;
    private LocalDateTime updatedAt;
}
