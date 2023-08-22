package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class CreateNewCommunityRequest {
    private String communityRef;
    private String communityName;
    private String authZeroId;
}
