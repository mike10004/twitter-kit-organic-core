/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.sdk.organic.core;



/**
 * Authorization configuration details.
 */
public class TwitterAuthConfig {

    private final String consumerKey;
    private final String consumerSecret;

    /**
     * @param consumerKey    The consumer key.
     * @param consumerSecret The consumer secret.
     *
     * @throws IllegalArgumentException if consumer key or consumer secret is null.
     */
    public TwitterAuthConfig(String consumerKey, String consumerSecret) {
        if (consumerKey == null || consumerSecret == null) {
            throw new IllegalArgumentException(
                    "TwitterAuthConfig must not be created with null consumer key or secret.");
        }
        this.consumerKey = sanitizeAttribute(consumerKey);
        this.consumerSecret = sanitizeAttribute(consumerSecret);
    }

    /**
     * @return the consumer key
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @return the consumer secret
     */
    public String getConsumerSecret() {
        return consumerSecret;
    }

    static String sanitizeAttribute(String input) {
        if (input != null) {
            return input.trim();
        } else {
            return null;
        }
    }

}
