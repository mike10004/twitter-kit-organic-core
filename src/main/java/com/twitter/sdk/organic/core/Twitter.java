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
 *  The {@link Twitter} class stores common configuration and state for TwitterKit SDK.
 */
public final class Twitter {
    public static final String TAG = "Twitter";
    static final Logger DEFAULT_LOGGER = new DefaultLogger();

    public Twitter(TwitterConfig config) {
    }

    /**
     * @return the global {@link Logger}.
     */
    public static Logger getLogger() {
        return DEFAULT_LOGGER;
    }
}
