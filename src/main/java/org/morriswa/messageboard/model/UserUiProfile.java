package org.morriswa.messageboard.model;

import java.util.UUID;

public record UserUiProfile (
     UUID userId,
     String theme
) {}
