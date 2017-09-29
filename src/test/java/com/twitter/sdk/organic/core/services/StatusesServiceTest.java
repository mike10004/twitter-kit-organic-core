package com.twitter.sdk.organic.core.services;

import com.twitter.sdk.organic.core.models.Tweet;
import org.junit.Test;
import retrofit2.Response;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StatusesServiceTest extends TwitterApiClientServiceTestBase {

    @Test
    public void timeline() throws Exception {
        handleOkJson(uri -> "/1.1/statuses/user_timeline.json".equals(uri.getPath()), session -> getClass().getResource("/user-timeline-1.json"));
        Response<List<Tweet>> response = play(newApi(), userTimelineCaller());
        assertEquals("num tweets", 2, response.body().size());
    }
}
