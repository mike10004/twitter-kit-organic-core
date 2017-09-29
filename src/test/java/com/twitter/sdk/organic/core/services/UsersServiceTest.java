package com.twitter.sdk.organic.core.services;

import com.twitter.sdk.organic.core.models.User;
import org.junit.Test;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

public class UsersServiceTest extends TwitterApiClientServiceTestBase {

    @Test
    public void show() throws Exception {
        handleOkJson(uri -> "/1.1/users/show.json".equals(uri.getPath()), session -> {
            String idParam = session.getParameters().get("screen_name").iterator().next();
            String resourcePath = String.format("/get-users-show-%s.json", idParam);
            return getClass().getResource(resourcePath);
        });
        String screenName = "twitterdev";
        CallProvider<User> callProvider = client -> client.getUsersService().show(screenName, null);
        Response<User> response = play(newApi(), callProvider);
        assertEquals("user id", 2244994945L, response.body().id);
    }

}
