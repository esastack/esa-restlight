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
package io.esastack.restlight.core.interceptor;

import esa.commons.spi.SPI;
import io.esastack.restlight.server.core.HttpRequest;

/**
 * This implementation of {@link InternalInterceptor} is used to match to a {@link HttpRequest}
 * which has a {@link HttpRequest#path()}(such as {@code /foo/bar})  by the {@link #includes()} and {@link
 * #excludes()}.
 *
 * @see InternalInterceptor
 * @see InterceptorFactory#of(HandlerInterceptor)
 */
@SPI
public interface HandlerInterceptor extends InternalInterceptor {

    String PATTERN_FOR_ALL = "/**";

    /**
     * The paths to apply the interceptor
     *
     * @return {@code null}  for all, or paths.
     */
    default String[] includes() {
        return null;
    }

    /**
     * The paths not allowed to apply the interceptor Note: excludes path has higher priority than includes path
     *
     * @return excludes
     */
    default String[] excludes() {
        return null;
    }

}
