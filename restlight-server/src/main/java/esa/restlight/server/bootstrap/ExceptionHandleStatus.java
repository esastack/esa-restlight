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
package esa.restlight.server.bootstrap;

import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;

/**
 * The {@link #handled} which represents the exception has been handled successfully and the {@link #retained}
 * which means the exception should been continue to be used after
 * {@link DispatcherExceptionHandler#handleException(AsyncRequest, AsyncResponse, Throwable)}ing are both
 * necessary as the handle result.
 */
public enum ExceptionHandleStatus {

    /**
     * The exception has been handled and shouldn't been continue to be used.
     */
    HANDLED_CLEAN(true, false),

    /**
     * The exception has been handled and is allowed to be continue used.
     */
    HANDLED_RETAINED(true, true),

    /**
     * The exception hasn't been handled and is allowed to be continue used.
     */
    UNHANDLED_RETAINED(false, true);

    /**
     * Whether the exception has been handled successfully or not.
     */
    private final boolean handled;

    /**
     * Whether the exception should been continue to be used after handling.
     */
    private final boolean retained;

    ExceptionHandleStatus(boolean handled, boolean retained) {
        this.handled = handled;
        this.retained = retained;
    }

    public boolean handled() {
        return this.handled;
    }

    public boolean retained() {
        return this.retained;
    }

}

