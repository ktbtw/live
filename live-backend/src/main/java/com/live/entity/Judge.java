package com.live.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Judge {
    private String id;
    private String name;
    private String role;
    private String avatar;
    private Integer leftVotes;
    private Integer rightVotes;
}
