package com.live.dto;

import com.live.entity.Judge;
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
public class JudgesConfig {
    private String streamId;
    private List<Judge> judges;
    private LocalDateTime updatedAt;
}
