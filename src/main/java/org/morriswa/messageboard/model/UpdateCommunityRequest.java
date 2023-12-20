package org.morriswa.messageboard.model;

import java.util.UUID;


public record UpdateCommunityRequest (

    Long communityId,

    String communityLocator,

    String communityDisplayName,

    UUID communityOwnerUserId
) { }
