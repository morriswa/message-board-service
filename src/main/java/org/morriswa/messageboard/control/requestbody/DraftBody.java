package org.morriswa.messageboard.control.requestbody;


public record DraftBody(
    String caption,
    String description
) { }