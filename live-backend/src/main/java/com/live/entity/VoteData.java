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
public class VoteData {
    private Integer leftVotes;
    private Integer rightVotes;
    private Integer totalVotes;
    private Integer leftPercentage;
    private Integer rightPercentage;
    private String streamId;
    private LocalDateTime updatedAt;
    
    public void calculatePercentages() {
        this.totalVotes = (leftVotes != null ? leftVotes : 0) + (rightVotes != null ? rightVotes : 0);
        if (totalVotes > 0) {
            this.leftPercentage = (int) Math.round((double) leftVotes / totalVotes * 100);
            this.rightPercentage = 100 - leftPercentage;
        } else {
            this.leftPercentage = 50;
            this.rightPercentage = 50;
        }
    }
}
