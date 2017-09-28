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

package com.twitter.sdk.organic.core.internal.oauth;


import com.twitter.sdk.organic.core.TwitterAuthToken;

/**
 * Represents an authorization response.
 */
public class OAuthResponse {

    /**
     * The authorization token. May be temporary (request token) or long-lived (access token).
     */
    public final TwitterAuthToken authToken;
    /**
     * The username associated with the access token.
     */
    public final String userName;
    /**
     * The user id associated with the access token.
     */
    public final long userId;

    public OAuthResponse(TwitterAuthToken authToken, String userName, long userId) {
        this.authToken = authToken;
        this.userName = userName;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("authToken=").append(authToken)
                .append(",userName=").append(userName)
                .append(",userId=").append(userId)
                .toString();
    }
}
