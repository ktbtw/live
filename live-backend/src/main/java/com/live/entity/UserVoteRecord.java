package com.live.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoteRecord {
    private String userId;
    private String streamId;
    private int leftVotes;
    private int rightVotes;
    private long updatedAt;
}
