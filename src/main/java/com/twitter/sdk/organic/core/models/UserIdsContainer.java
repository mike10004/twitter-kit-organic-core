package com.twitter.sdk.organic.core.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserIdsContainer extends Cursored {

    @SerializedName("ids")
    public final List<Long> ids;

    public UserIdsContainer(List<Long> ids, Long previousCursor, Long nextCursor) {
        super(previousCursor, nextCursor);
        this.ids = ids;
    }

}
