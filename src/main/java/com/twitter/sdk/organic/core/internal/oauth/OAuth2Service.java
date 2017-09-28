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

import com.twitter.sdk.organic.core.Callback;
import com.twitter.sdk.organic.core.Result;
import com.twitter.sdk.organic.core.Twitter;
import com.twitter.sdk.organic.core.TwitterAuthConfig;
import com.twitter.sdk.organic.core.TwitterCore;
import com.twitter.sdk.organic.core.TwitterException;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.internal.network.UrlUtils;

import okhttp3.OkHttpClient;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * OAuth2.0 service. Provides methods for requesting guest auth tokens.
 */
public class OAuth2Service extends OAuthService {
    OAuth2Api api;

    interface OAuth2Api {
        @POST("/1.1/guest/activate.json")
        Call<GuestTokenResponse> getGuestToken(
                @Header(OAuthConstants.HEADER_AUTHORIZATION) String auth);

        @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
        @FormUrlEncoded
        @POST("/oauth2/token")
        Call<OAuth2Token> getAppAuthToken(@Header(OAuthConstants.HEADER_AUTHORIZATION) String auth,
                                          @Field(OAuthConstants.PARAM_GRANT_TYPE) String grantType);
    }

    public OAuth2Service(TwitterCore twitterCore, TwitterApi api, OkHttpClient client) {
        super(twitterCore, api, client);
        this.api = getRetrofit().create(OAuth2Api.class);
    }

    /**
     * Requests an application-only auth token.
     *
     * @param callback The callback interface to invoke when when the request completes.
     */
    void requestAppAuthToken(final Callback<OAuth2Token> callback) {
        api.getAppAuthToken(getAuthHeader(), OAuthConstants.GRANT_TYPE_CLIENT_CREDENTIALS)
                .enqueue(callback);
    }

    private String getAuthHeader() {
        final TwitterAuthConfig authConfig = getTwitterCore().getAuthConfig();
        final ByteString string = ByteString.encodeUtf8(
                UrlUtils.percentEncode(authConfig.getConsumerKey())
                + ":"
                + UrlUtils.percentEncode(authConfig.getConsumerSecret()));

        return OAuthConstants.AUTHORIZATION_BASIC + " " + string.base64();
    }
}
