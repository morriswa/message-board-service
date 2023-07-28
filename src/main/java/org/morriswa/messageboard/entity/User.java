package org.morriswa.messageboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.morriswa.messageboard.model.UserRole;

import java.util.UUID;

@Entity @Table(name = "user_profile")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @NotBlank
    @Column(name="auth_zero_id", unique = true, nullable = false)
    private String authZeroId;

    @NotNull
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    @NotBlank
    @Column(name = "display_name", nullable = false, unique = true)
    private String displayName;

    @Email
    @Column(name="email",unique = true, nullable = false)
    private String email;
}
