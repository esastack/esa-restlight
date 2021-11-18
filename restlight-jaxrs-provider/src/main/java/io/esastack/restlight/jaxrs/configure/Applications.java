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
package io.esastack.restlight.jaxrs.configure;

import esa.commons.NetworkUtils;
import esa.commons.StringUtils;

import java.net.SocketAddress;
import java.net.URI;

public class Applications {

    private static URI baseUri;

    /**
     * Obtains the base uri of the application, which includes host, port etc.
     *
     * @return  uri
     */
    public static synchronized  URI baseUri() {
        if (baseUri == null) {
            throw new IllegalStateException("The baseUri is null, maybe the restlight haven't started!");
        }
        return baseUri;
    }

    static synchronized void baseUri(SocketAddress address, String contextPath, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        if (ssl) {
            sb.append("https");
        } else {
            sb.append("http");
        }
        sb.append("://");
        sb.append(NetworkUtils.parseAddress(address));
        if (StringUtils.isNotEmpty(contextPath)) {
            if (!contextPath.startsWith("/")) {
                sb.append("/");
            }
            sb.append(contextPath);
        }

        baseUri = URI.create(sb.toString());
    }

}
