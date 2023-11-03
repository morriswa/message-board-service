package org.morriswa.messageboard.model.validatedrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.morriswa.messageboard.model.UserRole;

@Valid @AllArgsConstructor @Getter
public class CreateUserRequest {
    @NotBlank
    private String authZeroId;

    @Email
    private String email;

    @NotBlank
    private String displayName;

    @NotNull
    private UserRole role;

}
