package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor @Getter
public class UserUiProfile {
    private UUID userId;
    private String theme;
}
