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
package io.esastack.restlight.jaxrs.impl.container;

import esa.commons.Checks;
import io.esastack.httpserver.core.RequestContext;
import io.esastack.restlight.jaxrs.impl.JaxrsContextUtils;
import io.esastack.restlight.jaxrs.impl.core.ModifiableMultivaluedMap;
import io.esastack.restlight.jaxrs.impl.core.UriInfoImpl;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractContainerRequestContext implements ContainerRequestContext {

    final RequestContext context;
    final UriInfoImpl uriInfo;
    private final Request request;
    private final HttpHeaders headers;

    private volatile boolean aborted;

    public AbstractContainerRequestContext(RequestContext context) {
        Checks.checkNotNull(context, "context");
        this.context = context;
        this.request = JaxrsContextUtils.getRequest(context);
        this.uriInfo = JaxrsContextUtils.getUriInfo(context);
        this.headers = JaxrsContextUtils.getHeaders(context);
    }

    @Override
    public Object getProperty(String name) {
        return context.getAttribute(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableList(Arrays.asList(context.attributeNames()));
    }

    @Override
    public void setProperty(String name, Object object) {
        if (object == null) {
            removeProperty(name);
        } else {
            context.setAttribute(name, object);
        }
    }

    @Override
    public void removeProperty(String name) {
        context.removeAttribute(name);
    }

    @Override
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public String getMethod() {
        return context.request().method().name();
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return new ModifiableMultivaluedMap(context.request().headers());
    }

    @Override
    public String getHeaderString(String name) {
        return headers.getHeaderString(name);
    }

    @Override
    public Date getDate() {
        return headers.getDate();
    }

    @Override
    public Locale getLanguage() {
        return headers.getLanguage();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return headers.getAcceptableLanguages();
    }

    @Override
    public int getLength() {
        return (int) context.request().contentLength();
    }

    @Override
    public MediaType getMediaType() {
        return headers.getMediaType();
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return headers.getAcceptableMediaTypes();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return headers.getCookies();
    }

    @Override
    public boolean hasEntity() {
        return context.request().body().readableBytes() > 0;
    }

    @Override
    public InputStream getEntityStream() {
        return context.request().inputStream();
    }

    @Override
    public SecurityContext getSecurityContext() {
        return JaxrsContextUtils.getSecurityContext(context);
    }

    @Override
    public void setSecurityContext(SecurityContext context) {
        JaxrsContextUtils.setSecurityContext(this.context, context);
    }

    @Override
    public void abortWith(Response response) {
        aborted = true;
        JaxrsContextUtils.setResponse(context, response);
    }

    public boolean isAborted() {
        return aborted;
    }

}

