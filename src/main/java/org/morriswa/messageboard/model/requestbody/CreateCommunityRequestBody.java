package org.morriswa.messageboard.model.requestbody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @AllArgsConstructor
public class CreateCommunityRequestBody {
    private String communityRef;
    private String communityName;
}
