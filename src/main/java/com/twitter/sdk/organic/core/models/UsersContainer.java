package com.twitter.sdk.organic.core.models;

import java.util.List;

public class UsersContainer extends Cursored {

    public final List<User> users;

    public UsersContainer(List<User> users, Long previousCursor, Long nextCursor) {
        super(previousCursor, nextCursor);
        this.users = users;
    }

}
