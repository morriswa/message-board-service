package org.morriswa.messageboard.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.UserRole;

import java.util.UUID;

@Getter @AllArgsConstructor
public class User {
    private final UUID userId;
    @JsonIgnore private final String authZeroId;
    private final String email;
    private final String displayName;
    private final UserRole role;
}
