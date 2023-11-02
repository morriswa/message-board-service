package org.morriswa.messageboard.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.morriswa.messageboard.model.UserRole;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class User {
    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @NotBlank
//    @Column(name="auth_zero_id", unique = true, nullable = false)
    private String authZeroId;

    @NotNull
//    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @NotBlank
//    @Column(name = "display_name", nullable = false, unique = true)
    private String displayName;

    @Email
//    @Column(name="email",unique = true, nullable = false)
    private String email;
}
