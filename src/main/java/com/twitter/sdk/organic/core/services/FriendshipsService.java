package com.twitter.sdk.organic.core.services;

import com.twitter.sdk.organic.core.models.RelationshipContainer;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FriendshipsService {

    @GET("/1.1/friendships/show.json")
    Response<RelationshipContainer> show(@Query("source_id") long sourceId, @Query("target_id") long targetId);

    @GET("/1.1/friendships/show.json")
    Response<RelationshipContainer> show(@Query("source_screen_name") String sourceScreenName, @Query("target_screen_name") String targetScreenName);

}
