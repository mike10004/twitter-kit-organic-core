package com.twitter.sdk.organic.core;

import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.models.Tweet;
import com.twitter.sdk.organic.core.services.TwitterApiClientServiceTestBase;
import com.twitter.sdk.organic.core.test.OauthCredentials;
import retrofit2.Response;

import java.io.File;
import java.util.List;

public class TwitterApiClientExample {

    /**
     * Reads a file {@code example-config.json} in the working directory and downloads the user
     * timeline.
     * @param args not used
     */
    public static void main(String[] args) throws Exception {
        File testConfigFile = new File(System.getProperty("user.dir"), "example-config.json");
        OauthCredentials consumer = OauthCredentials.loadFromFile(testConfigFile);
        Response<List<Tweet>> tweets = TwitterApiClientServiceTestBase.play(consumer, TwitterApi.createDefault(), TwitterApiClientServiceTestBase.userTimelineCaller());
        tweets.body().forEach(tweet -> {
            System.out.format("tweet %s: %s%n", tweet.id, tweet.text);
        });
    }

}
