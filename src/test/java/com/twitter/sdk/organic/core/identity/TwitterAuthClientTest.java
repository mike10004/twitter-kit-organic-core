package com.twitter.sdk.organic.core.identity;

import com.github.mike10004.twitter.organic.Uri;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.twitter.sdk.organic.core.test.NanoServer;
import com.twitter.sdk.organic.core.test.OauthConsumer;
import com.twitter.sdk.organic.core.Twitter;
import com.twitter.sdk.organic.core.TwitterAuthConfig;
import com.twitter.sdk.organic.core.TwitterConfig;
import com.twitter.sdk.organic.core.TwitterCore;
import com.twitter.sdk.organic.core.TwitterSession;
import com.twitter.sdk.organic.core.internal.TwitterApi;
import com.twitter.sdk.organic.core.internal.oauth.OAuthConstants;
import com.twitter.sdk.organic.core.test.TokenGenerator;
import fi.iki.elonen.NanoHTTPD;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TwitterAuthClientTest {

    private static final Joiner.MapJoiner mj = Joiner.on('&').withKeyValueSeparator('=');

    @Test
    public void authorize() throws Exception {
        Iterator<String> tokenGenerator = oauthTokenGenerator();
        Map<String, String> authzHeaderMap = new ConcurrentHashMap<>();
        Map<String, String> requestTokenToVerifier = new ConcurrentHashMap<>();
        try (NanoServer twitterApiServer = new NanoServer()) {
            twitterApiServer.handle(session -> {
                URI requestUri = URI.create(session.getUri());
                if (NanoHTTPD.Method.POST == session.getMethod()) {
                    if ("/oauth/request_token".equals(requestUri.getPath())) {
                        if (!isOauthAuthorizationHeaderPresent(session)) {
                            return badRequest("authorization header must be present for request token request");
                        }
                        String oauth_token = tokenGenerator.next();
                        authzHeaderMap.put(oauth_token, session.getHeaders().get("authorization"));
                        requestTokenToVerifier.put(oauth_token, tokenGenerator.next());
                        Map<String, String> responseContent = ImmutableMap.of(
                                "oauth_token", oauth_token,
                                "oauth_token_secret", "2kWe0NQe21Wzh1iR1MnH0130Yljfeew8",
                                "oauth_callback_confirmed", "true"
                        );
                        return NanoServer.response(200, HTML_UTF_8, mj.join(responseContent));
                    } else if ("/oauth/access_token".equals(requestUri.getPath())) {
                        if (!isOauthAuthorizationHeaderPresent(session)) {
                            return badRequest("authorization header must be present for access token request");
                        }
                        if (!session.getParameters().containsKey(OAuthConstants.PARAM_VERIFIER)) {
                            return badRequest("oauth_verifier query parameter must be present");
                        }
                        @SuppressWarnings("ConstantConditions")
                        String oauth_token = parseOAuthAuthorizationHeader(session.getHeaders().get("authorization")).get("oauth_token").stream().findFirst().get();
                        String submittedVerifier = getFirstValue(session.getParameters(), OAuthConstants.PARAM_VERIFIER);
                        String requiredVerifier = requestTokenToVerifier.get(oauth_token);
                        if (requiredVerifier == null) {
                            return badRequest("unknown request token");
                        }
                        if (requiredVerifier.equals(submittedVerifier)) {
                            String accessToken = tokenGenerator.next();
                            String accessTokenSecret = tokenGenerator.next();
                            String userId = "281468798901";
                            String screenName = "NotARealUser";
                            Map<String, String> responseContent = ImmutableMap.<String, String>builder()
                                    .put("oauth_token", accessToken)
                                    .put("oauth_token_secret", accessTokenSecret)
                                    .put("user_id", userId)
                                    .put("screen_name", screenName)
                                    .put("x_auth_expires", "0")
                                    .build();
                            return NanoServer.response(200, HTML_UTF_8, mj.join(responseContent));
                        } else {
                            return badRequest("invalid verifier");
                        }

                    } else if ("/login".equals(requestUri.getPath())) {
                        String oauth_token = getFirstValue(session.getParameters(), "oauth_token");
                        String authorizationHeader = authzHeaderMap.get(oauth_token);
                        if (authorizationHeader == null) {
                            return NanoServer.response(400, PLAIN_TEXT_UTF_8, String.format("no authorization header has been recorded for oauth_token=%s", oauth_token));
                        }
                        Multimap<String, String> oauthParams = parseOAuthAuthorizationHeader(authorizationHeader);
                        String verifierCode = requestTokenToVerifier.get(oauth_token);
                        @SuppressWarnings("ConstantConditions")
                        Uri redirectUri = Uri.parse(oauthParams.get("oauth_callback").stream().findAny().get())
                                .buildUpon()
                                .appendQueryParameter(OAuthConstants.PARAM_VERIFIER, verifierCode)
                                .build();
                        NanoHTTPD.Response response = NanoServer.response(REDIRECT_STATUS_CODE, PLAIN_TEXT_UTF_8, "");
                        response.addHeader("Location", redirectUri.toString());
                        System.out.format("redirecting to %s%n", redirectUri);
                        return response;
                    }
                } else if (NanoHTTPD.Method.GET == session.getMethod()) {
                    if("/oauth/authorize".equals(requestUri.getPath())) {
                        String oauth_token = getFirstValue(session.getParameters(), "oauth_token");
                        String html = "<!DOCTYPE html>" +
                                "<html>" +
                                "  <body>" +
                                "    <form method=\"post\" action=\"/login?oauth_token=" + oauth_token + "\">" +
                                "      <input type=\"submit\">" +
                                "    </form>" +
                                "  </body>" +
                                "</html>";
                        return NanoServer.response(200, HTML_UTF_8, html);
                    }
                }
                return null;
            });
            TwitterApi api = new TwitterApi("http://" + twitterApiServer.getSocketAddress() + "/");
            AuthzUrlCallback callback = urlResponse -> {
                String oauth_token = urlResponse.requestToken.token;
                // POST to /login?oauth_token=
                try (CloseableHttpClient client = HttpClients.createSystem()) {
                    Uri postUri = urlResponse.authorizationUri.buildUpon()
                            .path("/login")
                            .appendQueryParameter("oauth_token", oauth_token).build();
                    System.out.format("sending POST %s%n", postUri);
                    HttpPost preRedirectRequest = new HttpPost(URI.create(postUri.toString()));
                    Header redirectLocation;
                    try (CloseableHttpResponse response = client.execute(preRedirectRequest)) {
                        assertEquals("POST status", REDIRECT_STATUS_CODE, response.getStatusLine().getStatusCode());
                        redirectLocation = response.getFirstHeader(HttpHeaders.LOCATION);
                        System.out.format("received %s location header %s%n", response.getStatusLine().getStatusCode(), redirectLocation);
                    }
                    assertNotNull("redirect location header", redirectLocation);
                    URI redirectDestUrl = URI.create(redirectLocation.getValue());
                    HttpGet finalGet = new HttpGet(redirectDestUrl);
                    try (CloseableHttpResponse response = client.execute(finalGet)) {
                        assertEquals("GET status", 200, response.getStatusLine().getStatusCode());
                        System.out.format("received GET response %s: %s%n", response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
                    }
                }
            };
            OauthConsumer consumer = new OauthConsumer(tokenGenerator.next(), tokenGenerator.next());
            main0(consumer, api, callback, Duration.ZERO);
        }

    }

    private static final int REDIRECT_STATUS_CODE = 302;

    private static String getFirstValue(Map<String, List<String>> parameters, String key) {
        return parameters.getOrDefault(key, Collections.emptyList()).stream().findAny().orElse(null);
    }

    private static ImmutableMultimap<String, String> parseOAuthAuthorizationHeader(String value) {
        checkArgument(value != null, "expect non-null value");
        checkArgument(value.startsWith("OAuth "), "expected header value to start with [OAuth and end with ]: %s", value);
        value = StringUtils.removeStart(value, "OAuth ");
        List<String> tokens = Splitter.on(Pattern.compile(",\\s*")).splitToList(value);
        Pattern definitionPatt = Pattern.compile("(\\S+)=\"([^\"]+)\"");
        ImmutableMultimap.Builder<String, String> b = ImmutableMultimap.builder();
        tokens.forEach(token -> {
            Matcher m = definitionPatt.matcher(token);
            checkArgument(m.find(), "does not match definition pattern: %s", token);
            String key = m.group(1), encodedHeaderValue = m.group(2);
            String decodedHeaderValue = Uri.UriCodec.decode(encodedHeaderValue, false, UTF_8, true);
            b.put(key, decodedHeaderValue);
        });
        return b.build();
    }

    private static NanoHTTPD.Response badRequest(String msg) {
        return NanoServer.response(400, PLAIN_TEXT_UTF_8, msg);
    }

    private static boolean isOauthAuthorizationHeaderPresent(NanoHTTPD.IHTTPSession session) {
        return session.getHeaders().containsKey("authorization");
    }

    private static class EchoInterceptor implements Interceptor {

        private AtomicInteger counter = new AtomicInteger();

        @Override
        public Response intercept(Chain chain) throws IOException {
            int index = counter.incrementAndGet();
            Request request = chain.request();
            request = print(index, request);
            Response response = chain.proceed(request);
            response = print(index, response);
            return response;
        }

        private Request print(int index, Request request) throws IOException {
            RequestBody body = request.body();
            Buffer buff = new Buffer();
            body.writeTo(buff);
            byte[] bodyBytes = {};
            if ("POST".equalsIgnoreCase(request.method()) || "PUT".equalsIgnoreCase(request.method())) {
                bodyBytes = buff.readByteArray();
                RequestBody bodyCopy = RequestBody.create(body.contentType(), bodyBytes);
                request = request.newBuilder().method(request.method(), bodyCopy).build();
            }
            System.out.format("%d\trequest: %s %s (%d headers, body %s length %d)%n", index, request.method(), request.url().uri(), request.headers().size(), request.body().contentType(), bodyBytes.length);
            printHeaders(index, request.headers(), System.out);
            return request;
        }

        private void printHeaders(int index, Headers headers, PrintStream out) {
            headers.toMultimap().forEach((name, values) -> {
                values.forEach(value -> out.format("%d\t%s: %s%n", index, name, value));
            });
        }

        private Response print(int index, Response response) throws IOException {
            byte[] bodyBytes = response.body().bytes();
            System.out.format("%d\tresponse: %s %s; %d headers, body %s length %d: %s%n", index, response.code(), response.message(), response.headers().size(), response.body().contentType(), bodyBytes.length, describeBody(response.body().contentType(), bodyBytes));
            printHeaders(index, response.headers(), System.out);
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(), bodyBytes)).build();
        }

        private String describeBody(MediaType contentType, byte[] bytes) {
            if ("text".equals(contentType.type())) {
                return new String(bytes, contentType.charset(UTF_8));
            } else {
                return Base64.getEncoder().encodeToString(bytes);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        OauthConsumer consumer = OauthConsumer.loadFromFile(new File("./example-config.json"));
        main0(consumer, TwitterApi.createDefault(), u -> {}, Duration.ofSeconds(1));
    }

    private interface AuthzUrlCallback {
        void urlObtained(TwitterAuthClient.AuthorizationUriResponse response) throws Exception;
    }

    private static void main0(OauthConsumer consumer, TwitterApi api, AuthzUrlCallback callback, Duration waitBeforeServerShutdown) throws Exception {
        TwitterConfig twitterConfig = new TwitterConfig.Builder().build();
        Twitter twitter = new Twitter(twitterConfig);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(consumer.clientId, consumer.clientSecret);
        TwitterCore core = new TwitterCore(twitter, authConfig);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new EchoInterceptor())
                .build();
        TwitterAuthClient authClient = new TwitterAuthClient(core, authConfig, api, client);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TwitterSession> sessionRef = new AtomicReference<>();
        try (NanoServer redirectServer = new NanoServer()) {
            String path = "/redirect";
            Uri baseRedirectUri = Uri.parse("http://" + redirectServer.getSocketAddress() + path);
            System.out.format("base redirect uri: %s%n", baseRedirectUri);
            TwitterAuthClient.OAuthController ctrl = authClient.authorize(baseRedirectUri);
            TwitterAuthClient.AuthorizationUriResponse authorizationUriResponse = ctrl.requestAuthorizationUrl();
            redirectServer.handle(session -> {
                System.out.format("%s %s%n", session.getMethod(), session.getUri());
                if (path.equalsIgnoreCase(java.net.URI.create(session.getUri()).getPath())) {
                    Uri redirectedUri = Uri.parse(session.getUri() + "?" + session.getQueryParameterString());
                    TwitterSession twsession;
                    try {
                        twsession = ctrl.handleRedirect(authorizationUriResponse, redirectedUri);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        return NanoServer.response(500, PLAIN_TEXT_UTF_8, e.toString());
                    }
                    sessionRef.set(twsession);
                    latch.countDown();
                    return NanoServer.response(200, PLAIN_TEXT_UTF_8, String.format("app %s authorized by %s%n", consumer.clientId, twsession.getUserName()));
                }
                return null;
            });
            System.out.format("%s <-- visit this URL, log in, and authorize the consumer%n", authorizationUriResponse.authorizationUri);
            callback.urlObtained(authorizationUriResponse);
            latch.await(60, TimeUnit.SECONDS);
            Thread.sleep(waitBeforeServerShutdown.toMillis()); // wait for browser to receive response
        }
        TwitterSession session = sessionRef.get();
        assertNotNull("session", session);
        System.out.format("new session: user id = %s, screen name = %s%n", session.getUserId(), session.getUserName());
        System.out.format("new session: access token = %s, secret = %s%n", session.getAuthToken().token, session.getAuthToken().secret);

    }

    private static Iterator<String> oauthTokenGenerator() {
        return new TokenGenerator();
    }

}