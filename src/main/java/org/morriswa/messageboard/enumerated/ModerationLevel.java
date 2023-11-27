package org.morriswa.messageboard.enumerated;

public enum ModerationLevel {
    NONE(0),
    COMMENT_MOD(5),
    CONTENT_MOD(10),
    EDIT_MOD(15),
    PROMOTE_MOD(20);

    public final int weight;

    ModerationLevel(int weight) { this.weight = weight; }
}
