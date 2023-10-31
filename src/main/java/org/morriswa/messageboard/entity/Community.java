package org.morriswa.messageboard.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.GregorianCalendar;
import java.util.UUID;

//@Entity @Table(name = "community")
@NoArgsConstructor @Data @AllArgsConstructor
@Builder

public class Community {
//    @Id
//    @Column(name = "community_id")
//    @SequenceGenerator(name="community_id_seq_gen",sequenceName = "community_id_seq")
//    @GeneratedValue(generator = "community_id_seq_gen",strategy = GenerationType.AUTO)
    private Long communityId;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]*$")
//    @Column(name = "community_ref", nullable = false, unique = true)
    private String communityLocator;

    @NotBlank
//    @Column(name = "display_name", nullable = false)
    private String communityDisplayName;

    @NotNull
//    @Column(nullable = false)
    private UUID communityOwnerUserId;

    @NotNull
//    @Column(nullable = false, updatable = false)
    private GregorianCalendar dateCreated;


    public Community(String communityLocator, String communityDisplayName, UUID communityOwnerUserId) {
        this.communityLocator = communityLocator;
        this.communityDisplayName = communityDisplayName;
        this.communityOwnerUserId = communityOwnerUserId;
        this.dateCreated = new GregorianCalendar();
    }
}
