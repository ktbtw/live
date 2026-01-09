package com.live.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoteRequest {
    private String userId;
    private String streamId;
    private Integer leftVotes;
    private Integer rightVotes;
    private String mode;
    private UserVoteRequest request;
    
    public UserVoteRequest getEffectiveRequest() {
        return request != null ? request : this;
    }
}
