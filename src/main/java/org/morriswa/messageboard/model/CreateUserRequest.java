package org.morriswa.messageboard.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String authZeroId;

    @NotNull
    private UserRole role;

    @NotBlank
    private String displayName;

    @Email
    private String email;
}
