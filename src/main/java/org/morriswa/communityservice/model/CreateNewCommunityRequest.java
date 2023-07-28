package org.morriswa.communityservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class CreateNewCommunityRequest {
    private String communityName;
    private UUID userId;
}
