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
package io.esastack.restlight.core.util;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpStatus;

import java.util.Date;
import java.util.Objects;

public class ErrorDetail<T> {

    private final String path;
    private final T message;
    private final Date time;

    public ErrorDetail(String path, T message) {
        this.path = path;
        this.message = message;
        this.time = new Date();
    }

    public String getPath() {
        return path;
    }

    public T getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }

    public static String getMessage(HttpStatus status, Throwable th) {
        String message;
        if (StringUtils.isEmpty(message = th.getMessage())) {
            return status.reasonPhrase();
        } else {
            return message;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorDetail<?> that = (ErrorDetail<?>) o;
        return time == that.time && Objects.equals(path, that.path) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, message, time);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrorDetail{");
        sb.append("path='").append(path).append('\'');
        sb.append(", message=").append(message);
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}
