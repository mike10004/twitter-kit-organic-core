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
 * Configurable Twitter options
 */
public class TwitterConfig {

    private TwitterConfig() {
    }

    /**
     * Builder for creating {@link TwitterConfig} instances.
     * */
    public static class Builder {

        /**
         * Start building a new {@link TwitterConfig} instance.
         */
        public Builder() {
        }

        /**
         * Build the {@link TwitterConfig} instance
         */
        public TwitterConfig build() {
            return new TwitterConfig();
        }
    }
}
