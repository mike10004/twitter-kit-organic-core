/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.sdk.organic.core;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Twitter session that is associated with a {@link com.twitter.sdk.organic.core.TwitterAuthToken}.
 */
public class TwitterSession extends Session<TwitterAuthToken> {
    public static final long UNKNOWN_USER_ID = -1L;
    public static final String UNKNOWN_USER_NAME = "";

    @SerializedName("user_name")
    private final String userName;

    /**
     * @param authToken Auth token
     * @param userId    User ID
     * @param userName  User Name
     *
     * @throws IllegalArgumentException if token argument is null
     */
    public TwitterSession(TwitterAuthToken authToken, long userId, String userName) {
        super(authToken, userId);
        this.userName = userName;
    }

    public long getUserId() {
        return getId();
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final TwitterSession that = (TwitterSession) o;

        return userName != null ? userName.equals(that.userName) : that.userName == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }

}
