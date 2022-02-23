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
package io.esastack.restlight.jaxrs.impl.core;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.core.util.HttpHeaderUtils;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import io.esastack.restlight.server.util.LoggerUtils;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Collectors;

public class ResponseImpl extends Response {

    private static final IllegalStateException UNSUPPORTED_READ = new IllegalStateException("The read" +
            " operation is unsupported on server side!");

    private static final AtomicIntegerFieldUpdater<ResponseImpl> CLOSED_STATE = AtomicIntegerFieldUpdater
            .newUpdater(ResponseImpl.class, "closed");

    private final ResponseBuilderImpl builder;

    private volatile int closed = 0;

    ResponseImpl(ResponseBuilderImpl builder) {
        Checks.checkNotNull(builder, "builder");
        this.builder = builder;
    }

    public void setEntity(Object entity) {
        checkClosed();
        builder.entity(entity);
    }

    public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
        checkClosed();
        builder.entity(entity, annotations);
        builder.headers().putSingle(HttpHeaderNames.CONTENT_TYPE, mediaType);
    }

    public Annotation[] getAnnotations() {
        return builder.annotations();
    }

    public void setStatus(int status) {
        builder.status(status);
    }

    public void setStatus(Response.StatusType statusInfo) {
        builder.status(statusInfo);
    }

    @Override
    public int getStatus() {
        return builder.status().getStatusCode();
    }

    @Override
    public StatusType getStatusInfo() {
        return Status.fromStatusCode(getStatus());
    }

    @Override
    public Object getEntity() {
        checkClosed();
        return builder.entity();
    }

    @Override
    public <T> T readEntity(Class<T> entityType) {
        throw UNSUPPORTED_READ;
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType) {
        throw UNSUPPORTED_READ;
    }

    @Override
    public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
        throw UNSUPPORTED_READ;
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
        throw UNSUPPORTED_READ;
    }

    @Override
    public boolean hasEntity() {
        checkClosed();
        return builder.entity() != null;
    }

    @Override
    public boolean bufferEntity() {
        return false;
    }

    @Override
    public void close() {
        CLOSED_STATE.compareAndSet(this, 0, 1);
    }

    @Override
    public MediaType getMediaType() {
        Object mediaType = builder.headers().getFirst(HttpHeaders.CONTENT_TYPE);
        if (mediaType == null || mediaType instanceof MediaType) {
            return (MediaType) mediaType;
        } else if (mediaType instanceof io.esastack.commons.net.http.MediaType) {
            return MediaTypeUtils.convert((io.esastack.commons.net.http.MediaType) mediaType);
        } else {
            return MediaTypeUtils.convert(MediaTypeUtil.parseMediaType(mediaType.toString()));
        }
    }

    @Override
    public Locale getLanguage() {
        Object language = builder.headers().getFirst(HttpHeaders.CONTENT_LANGUAGE);
        if (language == null || language instanceof Locale) {
            return (Locale) language;
        } else {
            return HttpHeaderUtils.parseToLanguage(language.toString());
        }
    }

    @Override
    public int getLength() {
        Object contentLength = builder.headers().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLength == null) {
            return -1;
        }

        int length;
        try {
            length = Integer.parseInt(contentLength.toString());
        } catch (NumberFormatException ignore) {
            length = -1;
        }

        return length;
    }

    @Override
    public Set<String> getAllowedMethods() {
        List<Object> methods = builder.headers().get(HttpHeaders.ALLOW);
        if (methods == null) {
            return Collections.emptySet();
        } else {
            Set<String> methodSet = new HashSet<>();
            for (Object obj : methods) {
                methodSet.add(obj.toString());
            }
            return methodSet;
        }
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        List<Object> cookieValues = builder.headers().get(HttpHeaders.SET_COOKIE);
        if (cookieValues == null) {
            return Collections.emptyMap();
        } else {
            Map<String, NewCookie> cookies = new HashMap<>();
            for (Object item : cookieValues) {
                NewCookie newCookie = RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class)
                        .fromString(item.toString());
                cookies.put(newCookie.getName(), newCookie);
            }

            return cookies;
        }
    }

    @Override
    public EntityTag getEntityTag() {
        Object tag = builder.headers().getFirst(HttpHeaders.ETAG);
        if (tag == null || tag instanceof EntityTag) {
            return (EntityTag) tag;
        } else {
            return RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class).fromString(tag.toString());
        }
    }

    @Override
    public Date getDate() {
        Object data = builder.headers().getFirst(HttpHeaders.DATE);
        if (data == null || data instanceof Date) {
            return (Date) data;
        }
        return io.esastack.restlight.server.util.DateUtils.parseByCache(data.toString());
    }

    @Override
    public Date getLastModified() {
        Object data = builder.headers().getFirst(HttpHeaders.LAST_MODIFIED);
        if (data == null || data instanceof Date) {
            return (Date) data;
        } else {
            return io.esastack.restlight.server.util.DateUtils.parseByCache(data.toString());
        }
    }

    @Override
    public URI getLocation() {
        Object location = builder.headers().getFirst(HttpHeaders.LOCATION);
        if (location == null || location instanceof URI) {
            return (URI) location;
        } else {
            return URI.create(location.toString());
        }
    }

    @Override
    public Set<Link> getLinks() {
        Set<Link> links = new HashSet<>();
        List<Object> ls = builder.headers().get(HttpHeaders.LINK);
        if (ls != null) {
            for (Object item : ls) {
                if (item instanceof Link) {
                    links.add((Link) item);
                } else if (item instanceof String) {
                    links.add(Link.valueOf((String) item));
                } else {
                    LoggerUtils.logger().warn("Unrecognized header value of 'link': [{}]", item);
                }
            }
        }
        return links;
    }

    @Override
    public boolean hasLink(String relation) {
        return getLink(relation) != null;
    }

    @Override
    public Link getLink(String relation) {
        if (relation == null) {
            return null;
        }
        for (Link link : getLinks()) {
            if (link.getRel().equals(relation)) {
                return link;
            }
        }
        return null;
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        Link link = getLink(relation);
        return link == null ? null : Link.fromLink(link);
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return builder.headers();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return new StringHeadersMultivaluedMap(builder.headers());
    }

    @Override
    public String getHeaderString(String name) {
        return HttpHeaderUtils.concatHeaderValues(getStringHeaders().get(name));
    }

    private void checkClosed() {
        if (CLOSED_STATE.get(this) == 1) {
            throw new IllegalStateException("Response has been closed!");
        }
    }

    static class StringHeadersMultivaluedMap implements MultivaluedMap<String, String> {

        private final MultivaluedMap<String, Object> underlying;

        StringHeadersMultivaluedMap(MultivaluedMap<String, Object> underlying) {
            Checks.checkNotNull(underlying, "underlying");
            this.underlying = underlying;
        }

        @Override
        public void putSingle(String key, String value) {
            underlying.putSingle(key, value);
        }

        @Override
        public void add(String key, String value) {
            underlying.add(key, value);
        }

        @Override
        public String getFirst(String key) {
            return RuntimeDelegateUtils.toString(underlying.getFirst(key));
        }

        @Override
        public void addAll(String key, String... newValues) {
            underlying.addAll(key, newValues);
        }

        @Override
        public void addAll(String key, List<String> valueList) {
            List<Object> values = null;
            if (valueList != null) {
                values = new ArrayList<>(valueList);
            }
            underlying.addAll(key, values);
        }

        @Override
        public void addFirst(String key, String value) {
            underlying.addFirst(key, value);
        }

        @Override
        public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
            return RuntimeDelegateUtils.equalsIgnoreValueOrder(underlying, otherMap);
        }

        @Override
        public int size() {
            return underlying.size();
        }

        @Override
        public boolean isEmpty() {
            return underlying.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return underlying.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return underlying.containsValue(value);
        }

        @Override
        public List<String> get(Object key) {
            List<Object> previous = underlying.get(key);
            if (previous == null) {
                return null;
            }
            return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
        }

        @Override
        public List<String> put(String key, List<String> value) {
            final List<Object> newValue;
            if (value == null) {
                newValue = null;
            } else {
                newValue = new ArrayList<>(value);
            }
            List<Object> previous = underlying.put(key, newValue);
            if (previous == null) {
                return null;
            }
            return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
        }

        @Override
        public List<String> remove(Object key) {
            List<Object> previous = underlying.remove(key);
            if (previous == null) {
                return null;
            }
            return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
        }

        @Override
        public void putAll(Map<? extends String, ? extends List<String>> m) {
            Map<String, List<Object>> entries = new LinkedHashMap<>();
            m.forEach((k, v) -> entries.put(k, v == null ? null : new ArrayList<>(v)));
            underlying.putAll(entries);
        }

        @Override
        public void clear() {
            underlying.clear();
        }

        @Override
        public Set<String> keySet() {
            return underlying.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            Collection<List<String>> values = new ArrayList<>();
            underlying.values().forEach(vs -> {
                if (vs == null) {
                    values.add(null);
                } else {
                    values.add(vs.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList()));
                }
            });
            return values;
        }

        @Override
        public Set<Entry<String, List<String>>> entrySet() {
            Set<Entry<String, List<String>>> entries = new LinkedHashSet<>();
            underlying.forEach((key, value) -> entries.add(new Entry<String, List<String>>() {
                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public List<String> getValue() {
                    return value.stream().map(RuntimeDelegateUtils::toString)
                            .collect(Collectors.toList());
                }

                @Override
                public List<String> setValue(List<String> value) {
                    List<String> previous = getValue();
                    underlying.put(key, value == null ? null : new ArrayList<>(value));
                    return previous;
                }
            }));
            return entries;
        }
    }

}
