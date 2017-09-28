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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.internal.network.OkHttpClientHelper;
import com.twitter.sdk.organic.core.models.BindingValues;
import com.twitter.sdk.organic.core.models.BindingValuesAdapter;
import com.twitter.sdk.organic.core.models.SafeListAdapter;
import com.twitter.sdk.organic.core.models.SafeMapAdapter;
import com.twitter.sdk.organic.core.services.AccountService;
import com.twitter.sdk.organic.core.services.CollectionService;
import com.twitter.sdk.organic.core.services.ConfigurationService;
import com.twitter.sdk.organic.core.services.FavoriteService;
import com.twitter.sdk.organic.core.services.ListService;
import com.twitter.sdk.organic.core.services.MediaService;
import com.twitter.sdk.organic.core.services.SearchService;
import com.twitter.sdk.organic.core.services.StatusesService;

import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A class to allow authenticated access to Twitter API endpoints.
 * Can be extended to provided additional endpoints by extending and providing Retrofit API
 * interfaces to {@link com.twitter.sdk.organic.core.TwitterApiClient#getService(Class)}
 */
public class TwitterApiClient {

    private final ConcurrentHashMap<Class, Object> services;
    private final Retrofit retrofit;

    /**
     * Constructs User Session based TwitterApiClient. Builds an {@link OkHttpClient}
     * using {@link OkHttpClientHelper#getOkHttpClient(Session, TwitterAuthConfig)}.
     */
    public TwitterApiClient(TwitterAuthConfig authConfig, TwitterSession session, TwitterApi api) {
        this(OkHttpClientHelper.getOkHttpClient(session, authConfig), api);
    }

    /**
     * Constructs User Session based TwitterApiClient, with custom http client.
     *
     * The custom http client can be constructed with {@link okhttp3.Interceptor}, and other
     * optional params provided in {@link okhttp3.OkHttpClient}.
     */
    public TwitterApiClient(OkHttpClient client, TwitterApi twitterApi) {
        this.services = buildConcurrentMap();
        this.retrofit = buildRetrofit(client, twitterApi);
    }

    private Retrofit buildRetrofit(OkHttpClient httpClient, TwitterApi twitterApi) {
        return new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(twitterApi.getBaseHostUrl())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();
    }

    private Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new SafeListAdapter())
                .registerTypeAdapterFactory(new SafeMapAdapter())
                .registerTypeAdapter(BindingValues.class, new BindingValuesAdapter())
                .create();
    }

    private <K, V> ConcurrentHashMap<K, V> buildConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.AccountService} to access TwitterApi
     */
    public AccountService getAccountService() {
        return getService(AccountService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.FavoriteService} to access TwitterApi
     */
    public FavoriteService getFavoriteService() {
        return getService(FavoriteService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.StatusesService} to access TwitterApi
     */
    public StatusesService getStatusesService() {
        return getService(StatusesService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.SearchService} to access TwitterApi
     */
    public SearchService getSearchService() {
        return getService(SearchService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.ListService} to access TwitterApi
     */
    public ListService getListService() {
        return getService(ListService.class);
    }

    /**
     * Use CollectionTimeline directly, CollectionService is expected to change.
     * @return {@link CollectionService} to access TwitterApi
     */
    public CollectionService getCollectionService() {
        return getService(CollectionService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.ConfigurationService} to access TwitterApi
     */
    public ConfigurationService getConfigurationService() {
        return getService(ConfigurationService.class);
    }

    /**
     * @return {@link com.twitter.sdk.organic.core.services.MediaService} to access Twitter API
     * upload endpoints.
     */
    public MediaService getMediaService() {
        return getService(MediaService.class);
    }

    /**
     * Converts Retrofit style interface into instance for API access
     *
     * @param cls Retrofit style interface
     * @return instance of cls
     */
    @SuppressWarnings("unchecked")
    protected <T> T getService(Class<T> cls) {
        if (!services.contains(cls)) {
            services.putIfAbsent(cls, retrofit.create(cls));
        }
        return (T) services.get(cls);
    }
}
