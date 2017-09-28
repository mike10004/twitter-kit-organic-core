package com.twitter.sdk.organic.core;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class OauthConsumer {

    public String clientId;
    public String clientSecret;

    public OauthConsumer(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static OauthConsumer loadFromFile(File testConfigFile) throws IOException {
        try (Reader reader = new FileReader(testConfigFile)) {
            return new Gson().fromJson(reader, OauthConsumer.class);
        }
    }

}
