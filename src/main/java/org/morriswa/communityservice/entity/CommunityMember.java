package org.morriswa.communityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.morriswa.communityservice.model.CommunityStanding;

import java.util.GregorianCalendar;
import java.util.UUID;

@Entity @Table(name = "community_member")
@NoArgsConstructor @AllArgsConstructor
@Builder @Getter
public class CommunityMember {
    @Id
    @Column(name = "relationship_id")
    @SequenceGenerator(name="relationship_id_seq_gen",sequenceName = "relationship_id_seq")
    @GeneratedValue(generator = "relationship_id_seq_gen",strategy = GenerationType.AUTO)
    private Long relationshipId;

    @NotNull
    @Column(updatable = false, nullable = false)
    private Long communityId;

    @NotNull
    @Column(updatable = false, nullable = false)
    private UUID userId;

    @NotNull
    @Column(nullable = false)
    private Integer moderationLevel;

    @NotNull
    @Column(nullable = false)
    private CommunityStanding communityStanding;

    @NotNull
    @Column(nullable = false)
    private GregorianCalendar relationshipLastUpdatedDate;

    @NotNull
    @Column(nullable = false, updatable = false)
    private GregorianCalendar joinDate;

    public CommunityMember(UUID userId, Long communityId) {
        var date = new GregorianCalendar();
        this.relationshipLastUpdatedDate = date;
        this.joinDate = date;
        this.userId = userId;
        this.communityId = communityId;
        this.communityStanding = CommunityStanding.HEALTHY;
        this.moderationLevel = 0;
    }


}
