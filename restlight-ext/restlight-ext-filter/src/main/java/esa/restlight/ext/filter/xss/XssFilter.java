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
package esa.restlight.ext.filter.xss;

import esa.commons.Checks;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.httpserver.core.HttpInputStream;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.LoggerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

public class XssFilter implements Filter {

    private static final Pattern SCRIPT_TAGS_PATTERN =
            Pattern.compile("<[\r\n| ]*script[\r\n| ]*>(.*?)</[\r\n| ]*script[\r\n| ]*>",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_EXPRESSION_APOSTROPHE_PATTERN =
            Pattern.compile("src[\r\n]*=[\r\n]*'(.*?)'",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern SRC_EXPRESSION_QUOTA_PATTERN =
            Pattern.compile("src[\r\n]*=[\r\n]*\"(.*?)\"",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern LONESOME_SCRIPT_BACK_TAGS_PATTERN =
            Pattern.compile("</[\r\n| ]*script[\r\n| ]*>",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern LONESOME_SCRIPT_FACADE_TAGS_PATTERN =
            Pattern.compile("<[\r\n| ]*script(.*?)>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_EXPRESSION_PATTERN =
            Pattern.compile("eval\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXP_EXPRESSION_PATTERN =
            Pattern.compile("e­xpression\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_EXPRESSION_PATTERN =
            Pattern.compile("javascript[\r\n| ]*:[\r\n| ]*",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern ALTER_EXPRESSION_PATTERN =
            Pattern.compile("alert", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_EXPRESSION_PATTERN =
            Pattern.compile("onload(.*?)=",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern VB_SCRIPT_EXPRESSION_PATTERN =
            Pattern.compile("vbscript[\r\n| ]*:[\r\n| ]*",
                    Pattern.CASE_INSENSITIVE);

    private final Function<AsyncRequest, AsyncRequest> wrapper;

    public XssFilter(XssOptions options) {
        Checks.checkNotNull(options, "options");
        Checks.checkNotNull(options.getMode(), "XssMode");
        if (options.getMode() == XssMode.ESCAPE) {
            wrapper = EscapeWrapper::new;
        } else {
            wrapper = FilterWrapper::new;
        }
    }

    @Override
    public CompletableFuture<Void> doFilter(AsyncRequest request, AsyncResponse response, FilterChain chain) {
        return chain.doFilter(wrapper.apply(request), response);
    }

    abstract static class BaseWrapper implements AsyncRequest {

        Map<String, List<String>> parameterMap;
        final AsyncRequest delegate;

        BaseWrapper(AsyncRequest delegate) {
            this.delegate = delegate;
        }

        @Override
        public Map<String, List<String>> parameterMap() {
            // lazy load
            if (parameterMap == null) {
                Map<String, List<String>> parameterMapOrigin = delegate.parameterMap();
                if (parameterMapOrigin.isEmpty()) {
                    parameterMap = Collections.emptyMap();
                } else {
                    parameterMap = new HashMap<>(parameterMapOrigin.size());
                    List<String> params;
                    for (Map.Entry<String, List<String>> entry : parameterMapOrigin.entrySet()) {
                        final List<String> values = entry.getValue();
                        params = new ArrayList<>(values.size());
                        for (String param : values) {
                            params.add(handleParam(param));
                        }
                        parameterMap.put(entry.getKey(), params);
                    }
                }

            }
            return parameterMap;
        }

        abstract String handleParam(String param);

        // raw delegate method below

        @Override
        public List<String> getParameters(String parName) {
            return parameterMap().get(parName);
        }

        @Override
        public HttpVersion httpVersion() {
            return delegate.httpVersion();
        }

        @Override
        public String scheme() {
            return delegate.scheme();
        }

        @Override
        public HttpMethod method() {
            return delegate.method();
        }

        @Override
        public HttpInputStream inputStream() {
            return delegate.inputStream();
        }

        @Override
        public ByteBuf byteBufBody() {
            return delegate.byteBufBody();
        }

        @Override
        public String remoteAddr() {
            return delegate.remoteAddr();
        }

        @Override
        public String tcpSourceAddr() {
            return delegate.tcpSourceAddr();
        }

        @Override
        public int remotePort() {
            return delegate.remotePort();
        }

        @Override
        public String localAddr() {
            return delegate.localAddr();
        }

        @Override
        public int localPort() {
            return delegate.localPort();
        }

        @Override
        public String getParameter(String parName) {
            final List<String> params = getParameters(parName);
            if (params != null && params.size() > 0) {
                return params.get(0);
            }
            return null;
        }

        @Override
        public HttpHeaders headers() {
            return delegate.headers();
        }

        @Override
        public HttpHeaders trailers() {
            return delegate.trailers();
        }

        @Override
        public Set<Cookie> cookies() {
            return delegate.cookies();
        }

        @Override
        public Object getAttribute(String name) {
            return delegate.getAttribute(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            delegate.setAttribute(name, value);
        }

        @Override
        public Object removeAttribute(String name) {
            return delegate.removeAttribute(name);
        }

        @Override
        public String[] attributeNames() {
            return delegate.attributeNames();
        }

        @Override
        public ByteBufAllocator alloc() {
            return delegate.alloc();
        }
    }

    static class EscapeWrapper extends BaseWrapper {

        EscapeWrapper(AsyncRequest asyncRequest) {
            super(asyncRequest);
        }

        @Override
        public String uri() {
            return htmlEscape(delegate.uri());
        }

        @Override
        public String path() {
            return htmlEscape(delegate.path());
        }

        @Override
        public String query() {
            return htmlEscape(delegate.query());
        }

        @Override
        String handleParam(String param) {
            return htmlEscape(param);
        }

        @Override
        public String getHeader(CharSequence name) {
            return htmlEscape(delegate.getHeader(name));
        }
    }

    static class FilterWrapper extends BaseWrapper {

        FilterWrapper(AsyncRequest asyncRequest) {
            super(asyncRequest);
        }

        @Override
        public String uri() {
            return xssEncoder(delegate.uri());
        }

        @Override
        public String path() {
            return xssEncoder(delegate.path());
        }

        @Override
        public String query() {
            return xssEncoder(delegate.query());
        }

        @Override
        public String getHeader(CharSequence name) {
            return xssEncoder(delegate.getHeader(name));
        }

        @Override
        String handleParam(String param) {
            return xssEncoder(param);
        }
    }


    /**
     * Escape characters that can easily cause loopholes of xss
     */
    private static String htmlEscape(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        } else {
            StringBuilder result = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '<') {
                    result.append("&lt;");
                } else if (c == '>') {
                    result.append("&gt;");
                } else if (c == '"') {
                    result.append("&quot;");
                } else if (c == '&') {
                    result.append("&amp;");
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }

    }

    /**
     * Remove the statements with script and src etc., escape the replaced value
     */
    private static String xssEncoder(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                //'+' replace to '%2B'
                value = value.replace("+", "%2B");
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                LoggerUtils.logger().error("xss url decode error", e);
            }

            // Avoid null characters
            value = value.replaceAll("\0", "");

            // Avoid anything between script tags
            value = SCRIPT_TAGS_PATTERN.matcher(value).replaceAll("");

            // Avoid anything in a src='...' or src="..." type of expression
            value = SRC_EXPRESSION_APOSTROPHE_PATTERN.matcher(value).replaceAll("");
            value = SRC_EXPRESSION_QUOTA_PATTERN.matcher(value).replaceAll("");

            // Remove any lonesome </script> tag
            value = LONESOME_SCRIPT_BACK_TAGS_PATTERN.matcher(value).replaceAll("");

            // Remove any lonesome <script ...> tag
            value = LONESOME_SCRIPT_FACADE_TAGS_PATTERN.matcher(value).replaceAll("");

            // Avoid eval(...) expressions
            value = EVAL_EXPRESSION_PATTERN.matcher(value).replaceAll("");

            // Avoid e­xpression(...) expressions
            value = EXP_EXPRESSION_PATTERN.matcher(value).replaceAll("");

            // Avoid javascript:... expressions
            value = JAVASCRIPT_EXPRESSION_PATTERN.matcher(value).replaceAll("");

            // Avoid alert:... expressions
            value = ALTER_EXPRESSION_PATTERN.matcher(value).replaceAll("");

            // Avoid onload= expressions
            value = ONLOAD_EXPRESSION_PATTERN.matcher(value).replaceAll("");

            // Avoid vbscript:... expressions
            value = VB_SCRIPT_EXPRESSION_PATTERN.matcher(value).replaceAll("");
        }
        return value;
    }
}
