/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.jaxrs.util;

import io.esastack.commons.net.http.MediaType;

import java.util.concurrent.ConcurrentHashMap;

public final class MediaTypeUtils {

    private static final ConcurrentHashMap<jakarta.ws.rs.core.MediaType, ParseResult<MediaType>>
            JAKARTA_TO_COMMON_CACHE = new ConcurrentHashMap<>(16);

    private static final ConcurrentHashMap<MediaType, ParseResult<jakarta.ws.rs.core.MediaType>>
            COMMON_TO_JAKARTA_CACHE = new ConcurrentHashMap<>(16);

    public static MediaType convert(jakarta.ws.rs.core.MediaType from) {
        if (from == null) {
            return null;
        }
        ParseResult<MediaType> result = JAKARTA_TO_COMMON_CACHE.get(from);
        if (result == null) {
            result = JAKARTA_TO_COMMON_CACHE.computeIfAbsent(from, (key) -> {
                try {
                    return ParseResult.ok(MediaType.builder(key.getType(), key.getSubtype())
                            .addParams(key.getParameters()).build());
                } catch (Throwable th) {
                    return ParseResult.error(new IllegalArgumentException(th));
                }
            });
        }
        if (result.r != null) {
            return result.r;
        }
        throw result.t;
    }

    public static jakarta.ws.rs.core.MediaType convert(MediaType from) {
        if (from == null) {
            return null;
        }
        ParseResult<jakarta.ws.rs.core.MediaType> result = COMMON_TO_JAKARTA_CACHE.get(from);
        if (result == null) {
            result = COMMON_TO_JAKARTA_CACHE.computeIfAbsent(from, (key) -> {
                try {
                    return ParseResult.ok(new jakarta.ws.rs.core.MediaType(key.type(), key.subtype(), key.params()));
                } catch (Throwable th) {
                    return ParseResult.error(new IllegalArgumentException(th));
                }
            });
        }
        if (result.r != null) {
            return result.r;
        }
        throw result.t;
    }

    private static class ParseResult<T> {
        final T r;
        final IllegalArgumentException t;

        private ParseResult(T r, IllegalArgumentException t) {
            this.r = r;
            this.t = t;
        }

        static <T> ParseResult<T> ok(T r) {
            return new ParseResult<>(r, null);
        }

        static <T> ParseResult<T> error(IllegalArgumentException t) {
            return new ParseResult<>(null, t);
        }
    }

    private MediaTypeUtils() {
    }
}
