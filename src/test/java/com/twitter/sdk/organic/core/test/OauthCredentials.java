package com.twitter.sdk.organic.core.test;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class OauthCredentials extends OauthConsumer {

    public AccessBadge badge;

    public OauthCredentials(String clientId, String clientSecret, AccessBadge badge) {
        super(clientId, clientSecret);
        this.badge = badge;
    }

    public static class AccessBadge {
        public String accessToken;
        public String accessSecret;

        public AccessBadge(String accessToken, String accessSecret) {
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
        }
    }

    public static OauthCredentials create(String clientId, String clientSecret, String accessToken, String accessSecret) {
        return new OauthCredentials(clientId, clientSecret, new AccessBadge(accessToken, accessSecret));
    }

    public static OauthCredentials loadFromFile(File testConfigFile) throws IOException {
        try (Reader reader = new FileReader(testConfigFile)) {
            return new Gson().fromJson(reader, OauthCredentials.class);
        }
    }

}
