package org.morriswa.messageboard.model;


import lombok.Getter;

import java.net.URL;

@Getter
public class CommunityMemberResponse extends CommunityMember {

    private final URL profileImage;
    private final boolean isOwner;


    private CommunityMemberResponse(CommunityMember member, URL profileImage) {
        super(member.getUserId(),
                member.getDisplayName(), member.getEmail(),
                member.getModerationLevel(), member.getCommunityStanding(),
                member.getDateUpdated(), member.getDateJoined());
        this.profileImage = profileImage;
        this.isOwner = false;
    }

    private CommunityMemberResponse(UserProfileResponse owner) {
        super(owner.getUserId(),
                owner.getDisplayName(), owner.getEmail(),
                null, null,
                null, null);
        this.profileImage = owner.getUserProfileImage();
        this.isOwner = true;
    }

    public static CommunityMemberResponse buildMemberResponse(CommunityMember member, URL profileImage) {
        return new CommunityMemberResponse(member, profileImage);
    }

    public static CommunityMemberResponse buildOwnerResponse(UserProfileResponse owner) {
        return new CommunityMemberResponse(owner);
    }
}
