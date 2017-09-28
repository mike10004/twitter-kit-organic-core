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

import com.github.mike10004.twitter.organic.Uri;

import com.twitter.sdk.organic.core.TwitterAuthConfig;
import com.twitter.sdk.organic.core.TwitterAuthException;
import com.twitter.sdk.organic.core.TwitterAuthToken;
import com.twitter.sdk.organic.core.TwitterCore;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.internal.network.UrlUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * OAuth1.0a service. Provides methods for requesting request tokens, access tokens, and signing
 * requests.
 */
public class OAuth1aService extends OAuthService {

    interface OAuthApi {
        @POST("/oauth/request_token")
        Call<ResponseBody> getTempToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth);

        @POST("/oauth/access_token")
        Call<ResponseBody> getAccessToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth,
                                          @Query(OAuthConstants.PARAM_VERIFIER) String verifier);
    }

    private static final String RESOURCE_OAUTH = "oauth";
    private static final String PARAM_SCREEN_NAME = "screen_name";
    private static final String PARAM_USER_ID = "user_id";

    private OAuthApi api;
    private Uri baseRedirectUri;

    public OAuth1aService(TwitterCore twitterCore, TwitterApi api, OkHttpClient client, Uri baseRedirectUri) {
        super(twitterCore, api, client);
        this.baseRedirectUri = baseRedirectUri;
        this.api = getRetrofit().create(OAuthApi.class);
    }

    private static final Logger log = LoggerFactory.getLogger(OAuth1aService.class);

    /**
     * Requests a temp token to start the Twitter sign-in flow.
     *
     * @param callback The callback interface to invoke when the request completes.
     */
    public OAuthResponse requestTempToken() throws IOException, TwitterAuthException {
        final TwitterAuthConfig config = getTwitterCore().getAuthConfig();
        final String url = getTempTokenUrl();
        String authorizationHeader = new OAuth1aHeaders().getAuthorizationHeader(config, null,
                buildCallbackUrl(config), "POST", url, null);
        Response<ResponseBody> response = api.getTempToken(authorizationHeader).execute();
        if (response.isSuccessful()) {
            return transform(response.body());
        } else {
            log.warn("requesting request token failed", describeError(response));
            throw new TwitterAuthException("requesting request token failed");
        }
    }

    private static String describeError(Response<ResponseBody> response) {
        try {
            if (response.isSuccessful()) {
                return "<success>";
            } else {
                String errorBodyStr = response.errorBody().string();
                return String.format("%d: %s; %s", response.code(), response.message(), errorBodyStr);
            }
        } catch (RuntimeException | IOException e) {
            log.error("failed to describe error: " + response);
            return String.format("%d: %s; <error in describing>", response.code(), response.message());
        }
    }

    String getTempTokenUrl() {
        return getApi().getBaseHostUrl() + "/oauth/request_token";
    }

    /**
     * Builds a callback url that is used to receive a request containing the oauth_token and
     * oauth_verifier parameters.
     *
     * @param authConfig The auth config
     * @return the callback url
     */
    protected String buildCallbackUrl(TwitterAuthConfig authConfig) {
        return baseRedirectUri.buildUpon()
                .appendQueryParameter("version", getTwitterCore().getVersion())
                .appendQueryParameter("app", authConfig.getConsumerKey())
                .build()
                .toString();
    }

    /**
     * Requests a Twitter access token to act on behalf of a user account.
     */
    public OAuthResponse requestAccessToken(TwitterAuthToken requestToken, String verifier) throws IOException {
        final String url = getAccessTokenUrl();
        final String authHeader = new OAuth1aHeaders().getAuthorizationHeader(getTwitterCore()
                        .getAuthConfig(), requestToken, null, "POST", url, null);

        Response<ResponseBody> response = api.getAccessToken(authHeader, verifier).execute();
        if (response.isSuccessful()) {
            return transform(response.body());
        } else {
            log.warn("requesting access token failed", describeError(response));
            throw new TwitterAuthException("request access token failed");
        }
    }

    String getAccessTokenUrl() {
        return getApi().getBaseHostUrl() + "/oauth/access_token";
    }

    /**
     * @param requestToken The request token.
     * @return authorization url that can be used to get a verifier code to get access token.
     */
    public String getAuthorizeUrl(TwitterAuthToken requestToken) {
        // https://api.twitter.com/oauth/authorize?oauth_token=%s
        return getApi().buildUponBaseHostUrl(RESOURCE_OAUTH, "authorize")
                .appendQueryParameter(OAuthConstants.PARAM_TOKEN, requestToken.token)
                .build()
                .toString();
    }

    /**
     * @return  {@link OAuthResponse} parsed from the
     * response, may be {@code null} if the response does not contain an auth token and secret.
     */
    public static OAuthResponse parseAuthResponse(String response) throws TwitterAuthException {
        final TreeMap<String, String> params = UrlUtils.getQueryParams(response, false);
        final String token = params.get(OAuthConstants.PARAM_TOKEN);
        final String secret = params.get(OAuthConstants.PARAM_TOKEN_SECRET);
        final String userName = params.get(PARAM_SCREEN_NAME);
        final long userId;
        if (params.containsKey(PARAM_USER_ID)) {
            userId = Long.parseLong(params.get(PARAM_USER_ID));
        } else {
            userId = 0L;
        }
        if (token == null || secret == null) {
            throw new TwitterAuthException("token or secret is null");
        } else {
            return new OAuthResponse(new TwitterAuthToken(token, secret), userName, userId);
        }
    }

    protected OAuthResponse transform(ResponseBody responseBody) throws IOException {
        //Try to get response body
        final StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseBody.byteStream()))){
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        final String responseAsStr = sb.toString();
        final OAuthResponse authResponse = parseAuthResponse(responseAsStr);
        return authResponse;
    }

}
