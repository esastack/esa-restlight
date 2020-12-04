/*
 * Copyright 2020 OPPO ESA Stack Project
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
package esa.restlight.core.resolver.exception;

import esa.restlight.core.handler.Handler;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.util.Ordered;

/**
 * A ExceptionMapper is used to map an instance of {@link ExceptionResolver} to given type of error for the {@link
 * Handler} which should filter the {@link ExceptionMapper} by call the {@link #isApplicable(Handler)}.
 */
public interface ExceptionMapper extends Ordered {

    /**
     * Whether current {@link ExceptionMapper} is applicable to given {@link Handler}.
     *
     * @param handler handler
     *
     * @return {@code true} this {@link ExceptionMapper} is applicable, otherwise {@code false}
     */
    default boolean isApplicable(Handler handler) {
        return true;
    }

    /**
     * Maps a instance of {@link ExceptionResolver} to given target exception type.
     *
     * @param type target exception type
     *
     * @return mapped resolver, {@code null} if there's no resolver been mapped.
     */
    ExceptionResolver<Throwable> mapTo(Class<? extends Throwable> type);

}
