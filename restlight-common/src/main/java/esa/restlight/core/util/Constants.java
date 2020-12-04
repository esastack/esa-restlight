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
package esa.restlight.core.util;

public final class Constants {

    private Constants() {
    }

    @Deprecated
    public static final String SERVER_QUALIFIER = "Restlight-Server";
    @Deprecated
    public static final String MANAGEMENT_QUALIFIER = "Restlight-Management";

    public static final String INTERNAL = "$internal";
    public static final String SERVER = SERVER_QUALIFIER;
    public static final String MANAGEMENT = MANAGEMENT_QUALIFIER;

    public static final String MANAGEMENT_SERVER_PORT = "management.server.port";
    public static final String SERVER_PORT = "restlight.server.port";

    public static final String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

    public static final String DEFAULT_NEGOTIATION_FORMAT_PARAMETER = "format";
    public static final String NEGOTIATION_JSON_FORMAT = "json";
    public static final String NEGOTIATION_PROTO_BUF_FORMAT = "pb";
}
