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
package io.esastack.restlight.jaxrs.spi.headerdelegate;

import esa.commons.StringUtils;
import io.esastack.restlight.jaxrs.impl.core.LinkImpl;
import io.esastack.restlight.jaxrs.spi.HeaderDelegateFactory;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LinkDelegateFactory implements HeaderDelegateFactory {

    @Override
    public RuntimeDelegate.HeaderDelegate<?> headerDelegate() {
        return new LinkDelegate();
    }

    private static class LinkDelegate implements RuntimeDelegate.HeaderDelegate<Link> {

        @Override
        public Link fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null to Link");
            }
            String[] tokens = StringUtils.isEmpty(value) ? new String[0] : value.split(";");

            String uri = null;
            Map<String, String> params = new HashMap<>();
            for (String token : tokens) {
                String theToken = token.trim();
                if (theToken.startsWith("<") && theToken.endsWith(">")) {
                    uri = theToken.substring(1, theToken.length() - 1);
                } else {
                    int i = theToken.indexOf('=');
                    if (i != -1) {
                        String name = theToken.substring(0, i);
                        String value0 = theToken.substring(i + 1).replaceAll("\"", "");
                        params.put(name, value0);
                    }
                }
            }

            return uri == null ? null : new LinkImpl(URI.create(uri), params);
        }

        @Override
        public String toString(Link value) {
            if (value == null) {
                throw new IllegalArgumentException("Failed to parse a null(Link) to String");
            }
            final StringBuilder sb = new StringBuilder("<")
                    .append(value.getUri().toString())
                    .append(">");
            String rel = value.getRel();
            if (rel != null) {
                sb.append(";").append(Link.REL).append("=\"").append(rel).append("\"");
            }
            String title = value.getTitle();
            if (title != null) {
                sb.append(";").append(Link.TITLE).append("=\"").append(title).append("\"");
            }
            String type = value.getType();
            if (type != null) {
                sb.append(";").append(Link.TYPE).append("=\"").append(type).append("\"");
            }
            for (Map.Entry<String, String> entry : value.getParams().entrySet()) {
                String key = entry.getKey();
                if (Link.REL.equals(key) || Link.TITLE.equals(key) || Link.TYPE.equals(key)) {
                    continue;
                }
                sb.append(";").append(key).append("=\"").append(entry.getValue()).append("\"");
            }
            return sb.toString();
        }
    }
}

