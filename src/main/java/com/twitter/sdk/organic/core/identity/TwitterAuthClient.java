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

package com.twitter.sdk.organic.core.identity;

import com.github.mike10004.twitter.organic.Uri;
import com.twitter.sdk.organic.core.Twitter;
import com.twitter.sdk.organic.core.TwitterAuthConfig;
import com.twitter.sdk.organic.core.TwitterAuthException;
import com.twitter.sdk.organic.core.TwitterAuthToken;
import com.twitter.sdk.organic.core.TwitterCore;
import com.twitter.sdk.organic.core.TwitterSession;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.internal.oauth.OAuth1aService;
import com.twitter.sdk.organic.core.internal.oauth.OAuthConstants;
import com.twitter.sdk.organic.core.internal.oauth.OAuthResponse;
import okhttp3.OkHttpClient;

import java.io.IOException;

/**
 * Client for requesting authorization and email from the user.
 */
public class TwitterAuthClient {

    final TwitterCore twitterCore;
    final TwitterApi twitterApi;
    final TwitterAuthConfig authConfig;
    private final OkHttpClient httpClient;

    public TwitterAuthClient(TwitterCore twitterCore, TwitterAuthConfig authConfig,
                      TwitterApi twitterApi, OkHttpClient httpClient) {
        this.twitterCore = twitterCore;
        this.twitterApi = twitterApi;
        this.authConfig = authConfig;
        this.httpClient = httpClient;
    }

    public OAuthController authorize(Uri baseRedirectUri) {
        Twitter.getLogger().d(TwitterCore.TAG, "Using OAuth");
        OAuth1aService oauthService = new OAuth1aService(twitterCore, twitterApi, httpClient, baseRedirectUri);
        return new OAuthController(oauthService);
    }

    public static class AuthorizationUriResponse {
        public final TwitterAuthToken requestToken;
        public final Uri authorizationUri;

        public AuthorizationUriResponse(TwitterAuthToken requestToken, Uri authorizationUri) {
            this.requestToken = requestToken;
            this.authorizationUri = authorizationUri;
        }
    }

    public class OAuthController {

        private final OAuth1aService oAuth1aService;

        OAuthController(OAuth1aService oAuth1aService) {
            this.oAuth1aService = oAuth1aService;
        }

        public AuthorizationUriResponse requestAuthorizationUrl() throws IOException, TwitterAuthException {
            // Step 1. Obtain a request token to start the sign in flow.
            Twitter.getLogger().d(TwitterCore.TAG, "Obtaining request token to start the sign in flow");
            OAuthResponse tempTokenResponse = oAuth1aService.requestTempToken();
            TwitterAuthToken requestToken = tempTokenResponse.authToken;
            final String authorizeUrl = oAuth1aService.getAuthorizeUrl(requestToken);
            return new AuthorizationUriResponse(requestToken, Uri.parse(authorizeUrl));
        }

        /**
         * Requests an access token using the verification code present in URI and creates a new
         * session. As a side effect, the new session is set as the active session in the
         * session manager provided to {@link TwitterAuthClient()}.
         * @param authorizationUriResponse the return value of {@link #requestAuthorizationUrl()}
         * @param redirectedUri the URI to which the user was redirected after visiting the
         *                      authorization URI and authorizing the oauth client
         * @return the new session
         */
        public TwitterSession handleRedirect(AuthorizationUriResponse authorizationUriResponse, Uri redirectedUri) throws IOException, TwitterAuthException {
            Twitter.getLogger().d(TwitterCore.TAG, "requesting access token");
            final String verifier = redirectedUri.getQueryParameter(OAuthConstants.PARAM_VERIFIER);
            if (verifier != null) {
                // Step 3. Convert the request token to an access token.
                Twitter.getLogger().d(TwitterCore.TAG,
                        "Converting the request token to an access token.");
                OAuthResponse oauthResponse = oAuth1aService.requestAccessToken(authorizationUriResponse.requestToken, verifier);
                TwitterSession session = new TwitterSession(new TwitterAuthToken(oauthResponse.authToken.token, oauthResponse.authToken.secret), oauthResponse.userId, oauthResponse.userName);
                twitterCore.getSessionManager().setActiveSession(session);
                return session;
            }
            // If we get here, we failed to complete authorization.
            throw new TwitterAuthException("Failed to get authorization; uri does not contain query parameter " + OAuthConstants.PARAM_VERIFIER + ": " + redirectedUri);
        }

    }
}
