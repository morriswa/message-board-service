package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;

@Getter @AllArgsConstructor
public class Community {
    private final Long communityId;
    private final String communityLocator;
    private final String displayName;
    private final UUID ownerId;
    private final GregorianCalendar dateCreated;
    private final int communityMemberCount;
}
