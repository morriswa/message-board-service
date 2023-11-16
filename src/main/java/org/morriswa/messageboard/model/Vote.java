package org.morriswa.messageboard.model;

public enum Vote {
    UPVOTE(1),
    DOWNVOTE(-1),
    DELETE(0);

    public final int weight;

    Vote(int weight) {
        this.weight = weight;
    }
}
