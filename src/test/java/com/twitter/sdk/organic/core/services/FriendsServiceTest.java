package com.twitter.sdk.organic.core.services;

import com.twitter.sdk.organic.core.models.UserIdsContainer;
import org.junit.Test;
import retrofit2.Response;

import static org.junit.Assert.assertTrue;

public class FriendsServiceTest extends TwitterApiClientServiceTestBase {

    @Test
    public void ids() throws Exception {
        handleOkJson(uri -> "/1.1/friends/ids.json".equals(uri.getPath()), session -> getClass().getResource("/get-friends-ids-1.json"));
        Response<UserIdsContainer> response = play(newApi(), client -> client.getFriendsService().ids(123L, null, null));
        assertTrue("has known id", response.body().ids.contains(26123649L));
    }

}
