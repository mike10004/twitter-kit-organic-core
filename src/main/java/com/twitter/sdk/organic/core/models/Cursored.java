package com.twitter.sdk.organic.core.models;

import com.google.gson.annotations.SerializedName;

/**
 * Superclass for representations of responses that include cursor parameters.
 * See https://developer.twitter.com/en/docs/basics/cursoring.
 */
public abstract class Cursored {
    @SerializedName("previous_cursor")
    public final Long previousCursor;

    @SerializedName("next_cursor")
    public final Long nextCursor;

    public Cursored(Long previousCursor, Long nextCursor) {
        this.previousCursor = previousCursor;
        this.nextCursor = nextCursor;
    }
}
