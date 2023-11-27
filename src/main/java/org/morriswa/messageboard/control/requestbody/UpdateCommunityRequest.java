package org.morriswa.messageboard.control.requestbody;

import java.util.UUID;


public record UpdateCommunityRequest (

    Long communityId,

    String communityLocator,

    String communityDisplayName,

    UUID communityOwnerUserId
) { }
