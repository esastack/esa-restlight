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
import esa.commons.ClassUtils;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ContainerResponseContextImpl implements ContainerResponseContext {

    private final ResponseImpl response;
    private OutputStream outputStream;

    public ContainerResponseContextImpl(OutputStream outputStream, ResponseImpl response) {
        Checks.checkNotNull(outputStream, "outputStream");
        Checks.checkNotNull(response, "response");
        this.outputStream = outputStream;
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public void setStatus(int code) {
        response.setStatus(code);
    }

    @Override
    public Response.StatusType getStatusInfo() {
        return response.getStatusInfo();
    }

    @Override
    public void setStatusInfo(Response.StatusType statusInfo) {
        response.setStatus(statusInfo);
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return response.getHeaders();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return response.getStringHeaders();
    }

    @Override
    public String getHeaderString(String name) {
        return response.getHeaderString(name);
    }

    @Override
    public Set<String> getAllowedMethods() {
        return response.getAllowedMethods();
    }

    @Override
    public Date getDate() {
        return response.getDate();
    }

    @Override
    public Locale getLanguage() {
        return response.getLanguage();
    }

    @Override
    public int getLength() {
        return response.getLength();
    }

    @Override
    public MediaType getMediaType() {
        return response.getMediaType();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return response.getCookies();
    }

    @Override
    public EntityTag getEntityTag() {
        return response.getEntityTag();
    }

    @Override
    public Date getLastModified() {
        return response.getLastModified();
    }

    @Override
    public URI getLocation() {
        return response.getLocation();
    }

    @Override
    public Set<Link> getLinks() {
        return response.getLinks();
    }

    @Override
    public boolean hasLink(String relation) {
        return response.hasLink(relation);
    }

    @Override
    public Link getLink(String relation) {
        return response.getLink(relation);
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        return response.getLinkBuilder(relation);
    }

    @Override
    public boolean hasEntity() {
        return response.hasEntity();
    }

    @Override
    public Object getEntity() {
        return response.getEntity();
    }

    @Override
    public Class<?> getEntityClass() {
        return ClassUtils.getUserType(getEntity());
    }

    @Override
    public Type getEntityType() {
        return ClassUtils.getRawType(getEntityClass());
    }

    @Override
    public void setEntity(Object entity) {
        response.setEntity(entity);
    }

    @Override
    public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
        response.setEntity(entity, annotations, mediaType);
    }

    @Override
    public Annotation[] getEntityAnnotations() {
        return response.getAnnotations();
    }

    @Override
    public OutputStream getEntityStream() {
        return this.outputStream;
    }

    @Override
    public void setEntityStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}

