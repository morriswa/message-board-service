package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;


public record CommunityMember(
    UUID userId, String displayName, String email, ModerationLevel moderationLevel,
    CommunityStanding communityStanding, GregorianCalendar dateUpdated,
    GregorianCalendar dateJoined
) {
    public record Response (
        @JsonUnwrapped CommunityMember cm,
        URL profileImage,
        boolean isOwner
    ) { }

    public static CommunityMember.Response buildMemberResponse(CommunityMember member, URL profileImage) {
        return new CommunityMember.Response(member, profileImage, false);
    }

    public static CommunityMember.Response buildOwnerResponse(User.Response owner) {
        return new CommunityMember.Response(
                new CommunityMember(owner.user().userId(), owner.user().displayName(), owner.user().email(),
                        null, null, null, null),
                owner.userProfileImage(),
                true
        );
    }
}

