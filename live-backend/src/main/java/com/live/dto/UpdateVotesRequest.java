package com.live.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVotesRequest {
    private String streamId;
    private Integer leftVotes;
    private Integer rightVotes;
    private String action;
}
