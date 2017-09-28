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

import com.twitter.sdk.organic.core.internal.TwitterApi;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The TwitterCore Kit provides Login with Twitter and the Twitter API.
 */
public class TwitterCore {

    public static final String TAG = "Twitter";

    SessionManager<TwitterSession> twitterSessionManager;

    private final TwitterAuthConfig authConfig;
    private final ConcurrentHashMap<Session, TwitterApiClient> apiClients;

    public TwitterCore(Twitter twitter, TwitterAuthConfig authConfig, SessionManager<TwitterSession> authenticatedSessionManager) {
        this(twitter, authConfig, new ConcurrentHashMap<>(), authenticatedSessionManager);
    }

    // Testing only
    TwitterCore(Twitter twitter, TwitterAuthConfig authConfig,
                ConcurrentHashMap<Session, TwitterApiClient> apiClients,
                SessionManager<TwitterSession> authenticatedSessionManager) {
        this.authConfig = authConfig;
        this.apiClients = apiClients;
        this.twitterSessionManager = authenticatedSessionManager;
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_NUMBER;
    }

    public TwitterAuthConfig getAuthConfig() {
        return authConfig;
    }

    /* *********************************************************************************************
     *                      BEGIN PUBLIC API METHODS                                               *
     * *********************************************************************************************/

    /**
     * @return the {@link com.twitter.sdk.organic.core.SessionManager} for user sessions.
     */
    public SessionManager<TwitterSession> getSessionManager() {
        return twitterSessionManager;
    }

    /**
     * Creates {@link com.twitter.sdk.organic.core.TwitterApiClient} from authenticated
     * {@link com.twitter.sdk.organic.core.Session} provided.
     *
     * Caches internally for efficient access.
     * @param session the session
     */
    public TwitterApiClient getApiClient(TwitterSession session, TwitterApi api) {
        if (!apiClients.containsKey(session)) {
            apiClients.putIfAbsent(session, new TwitterApiClient(getAuthConfig(), session, api));
        }
        return apiClients.get(session);
    }

}
