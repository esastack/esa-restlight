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
package io.esastack.restlight.ext.filter.xss;

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.commons.net.http.Cookie;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.HttpVersion;
import io.esastack.commons.net.http.MediaType;
import io.esastack.httpserver.core.HttpInputStream;
import io.esastack.restlight.server.context.FilterContext;
import io.esastack.restlight.server.context.FilteringRequest;
import io.esastack.restlight.server.context.impl.FilterContextImpl;
import io.esastack.restlight.server.handler.FilterChain;
import io.esastack.restlight.server.spi.Filter;
import io.esastack.restlight.server.util.LoggerUtils;

import java.io.InputStream;
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

    private final Function<FilteringRequest, FilteringRequest> wrapper;

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
    public CompletableFuture<Void> doFilter(FilterContext context, FilterChain<FilterContext> chain) {
        return chain.doFilter(new FilterContextImpl(wrapper.apply(context.request()),
                context.response()));
    }

    abstract static class BaseWrapper implements FilteringRequest {

        Map<String, List<String>> parameterMap;
        final FilteringRequest delegate;

        BaseWrapper(FilteringRequest delegate) {
            this.delegate = delegate;
        }

        @Override
        public Map<String, List<String>> paramsMap() {
            // lazy load
            if (parameterMap == null) {
                Map<String, List<String>> parameterMapOrigin = delegate.paramsMap();
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
        public List<String> getParams(String parName) {
            return paramsMap().get(parName);
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
        public Buffer body() {
            return delegate.body();
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
        public String getParam(String parName) {
            final List<String> params = getParams(parName);
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
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public List<MediaType> accepts() {
            return delegate.accepts();
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
        public Object alloc() {
            return delegate.alloc();
        }

        @Override
        public String uri() {
            return delegate.uri();
        }

        @Override
        public String path() {
            return delegate.path();
        }

        @Override
        public String query() {
            return delegate.query();
        }

        @Override
        public void method(HttpMethod method) {
            delegate.method(method);
        }

        @Override
        public void uri(String uri) {
            delegate.uri(uri);
        }

        @Override
        public void body(byte[] body) {
            delegate.body(body);
        }

        @Override
        public void body(Buffer body) {
            delegate.body(body);
        }

        @Override
        public void inputStream(InputStream ins) {
            delegate.inputStream(ins);
        }
    }

    static class EscapeWrapper extends BaseWrapper {

        EscapeWrapper(FilteringRequest request) {
            super(request);
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

        FilterWrapper(FilteringRequest request) {
            super(request);
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
