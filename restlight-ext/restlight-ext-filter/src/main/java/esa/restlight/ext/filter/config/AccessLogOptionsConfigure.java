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
package esa.restlight.ext.filter.config;

/**
 * @deprecated use {@link esa.restlight.ext.filter.accesslog.AccessLogOptionsConfigure}
 */
@Deprecated
public final class AccessLogOptionsConfigure {

    private boolean fullUri;

    private AccessLogOptionsConfigure() {
    }

    public static AccessLogOptions defaultOpts() {
        return newOpts().configured();
    }

    public static AccessLogOptionsConfigure newOpts() {
        return new AccessLogOptionsConfigure();
    }

    public AccessLogOptionsConfigure fullUri(boolean fullUri) {
        this.fullUri = fullUri;
        return this;
    }

    public AccessLogOptions configured() {
        AccessLogOptions accessLogOptions = new AccessLogOptions();
        accessLogOptions.setFullUri(fullUri);
        return accessLogOptions;
    }
}
