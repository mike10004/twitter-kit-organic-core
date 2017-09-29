package com.twitter.sdk.organic.core.services;

import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.organic.core.Twitter;
import com.twitter.sdk.organic.core.TwitterApiClient;
import com.twitter.sdk.organic.core.TwitterAuthConfig;
import com.twitter.sdk.organic.core.TwitterAuthToken;
import com.twitter.sdk.organic.core.TwitterConfig;
import com.twitter.sdk.organic.core.TwitterCore;
import com.twitter.sdk.organic.core.TwitterException;
import com.twitter.sdk.organic.core.TwitterSession;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.models.Tweet;
import com.twitter.sdk.organic.core.models.User;
import com.twitter.sdk.organic.core.models.UserIdsContainer;
import com.twitter.sdk.organic.core.test.NanoHttpdRule;
import com.twitter.sdk.organic.core.test.NanoServer;
import com.twitter.sdk.organic.core.test.OauthCredentials;
import com.twitter.sdk.organic.core.test.TokenGenerator;
import fi.iki.elonen.NanoHTTPD;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TwitterApiClientServiceTestBase {

    private static final boolean verbose = true;

    @Rule
    public NanoHttpdRule nano = new NanoHttpdRule();

    protected static void dumpJson(Response<?> response) {
        if (verbose) {
            new GsonBuilder().setPrettyPrinting().create().toJson(response.body(), System.out);
            System.out.println();
        }
    }

    protected TwitterApi newApi() {
        return new TwitterApi("http://" + nano.getSocketAddress());
    }

    protected void handleOkJson(Predicate<? super URI> uriPredicate, Function<? super NanoHTTPD.IHTTPSession, URL> resourceTransform) {
        handleOk(uriPredicate, resourceTransform, MediaType.JSON_UTF_8);
    }

    protected void handleOk(Predicate<? super URI> uriPredicate, Function<? super NanoHTTPD.IHTTPSession, URL> resourceTransform, MediaType contentType) {
        nano.handle(session -> {
            URI uri = nano.toUri(session);
            if (uriPredicate.test(uri)) {
                URL resource = resourceTransform.apply(session);
                if (resource != null) {
                    String content = Resources.toString(resource, UTF_8);
                    return NanoServer.response(SC_OK, contentType, content);
                }
            }
            return null;
        });
    }

    public static CallProvider<List<Tweet>> userTimelineCaller() {
        return client -> client.getStatusesService().userTimeline(null, null, null, null, null, null, null, null, null);
    }

    protected interface CallProvider<T> {
        Call<T> provide(TwitterApiClient client) throws IOException, TwitterException;
    }

    protected static <T> Response<T> play(TwitterApi api, CallProvider<T> callProvider) throws IOException {
        TokenGenerator tokenGen = new TokenGenerator();
        OauthCredentials consumer = OauthCredentials.create(tokenGen.next(), tokenGen.next(), tokenGen.next(), tokenGen.next());
        return play(consumer, api, callProvider);
    }

    public static <T> Response<T> play(OauthCredentials consumer, TwitterApi api, CallProvider<T> callProvider) throws IOException {
        TwitterConfig config = new TwitterConfig.Builder().build();
        Twitter twitter = new Twitter(config);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(consumer.clientId, consumer.clientSecret);
        TwitterCore core = new TwitterCore(twitter, authConfig);
        TwitterAuthToken token = new TwitterAuthToken(consumer.badge.accessToken, consumer.badge.accessSecret);
        TwitterSession session = new TwitterSession(token, TwitterSession.UNKNOWN_USER_ID, TwitterSession.UNKNOWN_USER_NAME);
        TwitterApiClient client = core.getApiClient(session, api);
        Call<T> call = callProvider.provide(client);
        Response<T> response = call.execute();
        System.out.format("response status %s%n", response.code());
        assertTrue("success", response.isSuccessful());
        dumpJson(response);
        return response;
    }
}