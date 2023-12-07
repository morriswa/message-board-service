package org.morriswa.messageboard.validation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.UserRole;

import java.time.LocalDate;

@Valid @AllArgsConstructor @Getter
public class CreateUserRequest {

    @NotBlank
    private String authZeroId;

    @Email
    private String email;

    @NotNull
    private LocalDate birthdate;

    @NotBlank
    private String displayName;

    @NotNull
    private UserRole role;
}
