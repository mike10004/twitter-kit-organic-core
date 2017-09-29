package com.twitter.sdk.organic.core.services;

import com.twitter.sdk.organic.core.models.UserIdsContainer;
import com.twitter.sdk.organic.core.models.UsersContainer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FriendsService {

    @GET("/1.1/friends/ids.json")
    Call<UserIdsContainer> ids(@Query("user_id") long userId, @Query("cursor") Long cursor, @Query("count") Integer count);

    @GET("/1.1/friends/list.json")
    Call<UsersContainer> list(@Query("user_id") long userId, @Query("cursor") Long cursor, @Query("count") Integer count, @Query("skip_status") Boolean skipStatus, @Query("include_user_entities") Boolean includeUserEntities);

    @GET("/1.1/friends/list.json")
    Call<UsersContainer> list(@Query("screen_name") String screenName, @Query("cursor") Long cursor, @Query("count") Integer count, @Query("skip_status") Boolean skipStatus, @Query("include_user_entities") Boolean includeUserEntities);

}
