package org.morriswa.messageboard.model.requestbody;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Builder @Getter
public class UpdateUIProfileRequest {
    private String theme;
}
