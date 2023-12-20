package org.morriswa.messageboard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.morriswa.messageboard.enumerated.UserRole;

import java.time.LocalDate;

@Valid
public record CreateUserRequest(
    @NotBlank String authZeroId,
    @Email String email,
    @NotNull LocalDate birthdate,
    @NotBlank String displayName,
    @NotNull UserRole role
) {
    public record Body(String displayName, String birthdate) { }
}
