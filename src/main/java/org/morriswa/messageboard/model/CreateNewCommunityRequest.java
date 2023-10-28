package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CreateNewCommunityRequest {
    private String communityRef;
    private String communityName;
}
