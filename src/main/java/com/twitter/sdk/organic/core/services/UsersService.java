package com.twitter.sdk.organic.core.services;

import com.google.common.base.Joiner;
import com.twitter.sdk.organic.core.models.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface UsersService {
    /**
     * Returns a representation of a user.
     *
     * @param userId (required) id of the user
     */
    @GET("/1.1/users/show.json")
    Call<User> show(@Query("user_id") long userId, @Query("include_entities") Boolean includeEntities);

    /**
     * Returns a representation of a user.
     *
     * @param screenName (required) screen name of the user
     */
    @GET("/1.1/users/show.json")
    Call<User> show(@Query("screen_name") String screenName, @Query("include_entities") Boolean includeEntities);

    @GET("/1.1/users/lookup.json")
    Call<List<User>> lookupByScreenName(@Query("screen_name") String commaDelimitedScreenNames, @Query("include_entities") Boolean includeEntities);

    @GET("/1.1/users/lookup.json")
    Call<List<User>> lookupById(@Query("user_id") String commaDelimitedUserIds, @Query("include_entities") Boolean includeEntities);

    default Call<List<User>> lookupByScreenName(List<String> screenNames, Boolean includeEntities) {
        return lookupByScreenName(Joiner.on(',').join(screenNames), includeEntities);
    }

    default Call<List<User>> lookupById(List<Long> userIds, Boolean includeEntities) {
        return lookupById(Joiner.on(',').join(userIds), includeEntities);
    }
}
