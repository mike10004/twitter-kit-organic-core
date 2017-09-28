package com.twitter.sdk.organic.core;

import com.github.mike10004.twitter.organic.TwitterSessions;
import com.google.common.io.Resources;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.models.Tweet;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TwitterApiClientTest {

    @Rule
    public NanoHttpdRule nano = new NanoHttpdRule();

    @Test
    public void timeline() throws Exception {
        String tweetsJson = Resources.toString(getClass().getResource("/user-timeline-1.json"), UTF_8);
        nano.handle(session -> {
            System.out.format("URL: %s%n", session.getUri());
            session.getHeaders().forEach((key, value) -> System.out.format("%s: %s%n", key, value));
            return NanoServer.response(200, "application/json", tweetsJson);
        });

        TwitterApi api = new TwitterApi("http://" + nano.getSocketAddress());
        Response<List<Tweet>> tweets = play(OauthCredentials.create("abc", "def", "ghi", "jkl"), api);
        assertEquals("num tweets", 2, tweets.body().size());
    }

    /**
     * Reads a file {@code example-config.json} in the working directory and downloads the user
     * timeline.
     * @param args not used
     */
    public static void main(String[] args) throws Exception {
        File testConfigFile = new File(System.getProperty("user.dir"), "example-config.json");
        OauthCredentials consumer = OauthCredentials.loadFromFile(testConfigFile);
        play(consumer, TwitterApi.createDefault());
    }

    private static Response<List<Tweet>> play(OauthCredentials consumer, TwitterApi api) throws IOException {
        TwitterConfig config = new TwitterConfig.Builder().build();
        Twitter twitter = new Twitter(config);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(consumer.clientId, consumer.clientSecret);
        TwitterCore core = new TwitterCore(twitter, authConfig, TwitterSessions.inMemorySessionManager());
        TwitterAuthToken token = new TwitterAuthToken(consumer.badge.accessToken, consumer.badge.accessSecret);
        TwitterSession session = new TwitterSession(token, TwitterSession.UNKNOWN_USER_ID, TwitterSession.UNKNOWN_USER_NAME);
        TwitterApiClient client = core.getApiClient(session, api);
        Call<List<Tweet>> call = client.getStatusesService().userTimeline(null, null, null, null, null, null, null, null, null);
        Response<List<Tweet>> response = call.execute();
        System.out.format("response status %s%n", response.code());
        assertTrue("success", response.isSuccessful());
        response.body().forEach(tweet -> {
            System.out.format("tweet %s: %s%n", tweet.id, tweet.text);
        });
        return response;
    }
}