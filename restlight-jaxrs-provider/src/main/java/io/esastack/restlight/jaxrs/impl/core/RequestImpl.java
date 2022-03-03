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
import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.commons.net.http.MediaType;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restlight.jaxrs.util.MediaTypeUtils;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.util.HttpHeaderUtils;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static io.esastack.commons.net.http.HttpHeaderNames.IF_MATCH;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_MODIFIED_SINCE;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_NONE_MATCH;
import static io.esastack.commons.net.http.HttpHeaderNames.IF_UNMODIFIED_SINCE;
import static io.esastack.commons.net.http.HttpHeaderNames.VARY;
import static io.esastack.commons.net.http.HttpStatus.PRECONDITION_FAILED;

public class RequestImpl implements Request {

    private final HttpRequest request;
    private final HttpResponse response;

    private String varyHeader;

    public RequestImpl(RequestContext context) {
        Checks.checkNotNull(context, "context");
        this.request = context.request();
        this.response = context.response();
    }

    @Override
    public String getMethod() {
        return request.rawMethod();
    }

    @Override
    public Variant selectVariant(List<Variant> variants) {
        Checks.checkNotEmptyArg(variants, "variants");
        varyHeader = createVaryHeader(variants);
        response.headers().add(VARY, varyHeader);
        return VariantMatcher.INSTANCE.match(request, variants);
    }

    @Override
    public Response.ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        Checks.checkArg(eTag != null, "eTage");

        Response.ResponseBuilder builder = null;
        List<String> ifMatches = request.headers().getAll(IF_MATCH);
        if (ifMatches != null && !ifMatches.isEmpty()) {
            builder = ifMatch(toETags(ifMatches), eTag);
        }

        if (builder == null) {
            List<String> ifNoneMatches = request.headers().getAll(IF_NONE_MATCH);
            if (ifNoneMatches != null && !ifNoneMatches.isEmpty()) {
                builder = ifNoneMatch(toETags(ifNoneMatches), eTag);
            }
        }

        if (builder != null) {
            builder.tag(eTag);
        }

        return addVaryHeader(builder);
    }

    @Override
    public Response.ResponseBuilder evaluatePreconditions(Date lastModified) {
        Checks.checkArg(lastModified != null, "lastModified");

        Response.ResponseBuilder builder = null;
        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !request.headers().contains(IF_NONE_MATCH)) {
            builder = ifModifiedSince(ifModifiedSince, lastModified);
        }

        if (builder == null) {
            String ifUnmodifiedSince = request.headers().get(IF_UNMODIFIED_SINCE);
            if (ifUnmodifiedSince != null && !request.headers().contains(IF_MATCH)) {
                builder = ifUnmodifiedSince(ifUnmodifiedSince, lastModified);
            }
        }

        return addVaryHeader(builder);
    }

    @Override
    public Response.ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        Response.ResponseBuilder rstBuilder;
        final Response.ResponseBuilder eTageBuilder = evaluatePreconditions(eTag);
        final Response.ResponseBuilder lastModifiedBuilder = evaluatePreconditions(lastModified);
        if (eTageBuilder == null && lastModifiedBuilder == null) {
            return null;
        } else if (eTageBuilder != null && lastModifiedBuilder == null) {
            rstBuilder = eTageBuilder;
        } else if (eTageBuilder == null) {
            rstBuilder = lastModifiedBuilder;
        } else {
            rstBuilder = lastModifiedBuilder;
            rstBuilder.tag(eTag);
        }

        return addVaryHeader(rstBuilder);
    }

    @Override
    public Response.ResponseBuilder evaluatePreconditions() {
        List<String> ifMatches = request.headers().getAll(IF_MATCH);
        if (ifMatches == null || ifMatches.isEmpty()) {
            return null;
        }

        return Response.status(PRECONDITION_FAILED.code());
    }

    private Response.ResponseBuilder addVaryHeader(Response.ResponseBuilder builder) {
        if (builder != null && varyHeader != null) {
            builder.header(VARY, varyHeader);
        }
        return builder;
    }

    private Response.ResponseBuilder ifNoneMatch(List<EntityTag> ifNoneMatch, EntityTag eTag) {
        boolean match = false;

        for (EntityTag tag : ifNoneMatch) {
            if (tag.equals(eTag) || tag.getValue().equals("*")) {
                match = true;
                break;
            }
        }

        if (match) {
            HttpMethod method = request.method();
            if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
                return Response.notModified(eTag);
            }

            return Response.status(PRECONDITION_FAILED.code());
        }

        return null;
    }

    private static Response.ResponseBuilder ifMatch(List<EntityTag> ifMatch, EntityTag eTag) {
        for (EntityTag tag : ifMatch) {
            if (tag.equals(eTag) || tag.getValue().equals("*")) {
                return null;
            }
        }

        return Response.status(PRECONDITION_FAILED.code());
    }

    private static List<EntityTag> toETags(List<String> eTags) {
        final List<EntityTag> tags = new LinkedList<>();

        RuntimeDelegate.HeaderDelegate<EntityTag> delegate = RuntimeDelegate.getInstance()
                .createHeaderDelegate(EntityTag.class);
        if (delegate == null) {
            throw new IllegalStateException("Failed to get HeaderDelegate to resolve EntityTag.");
        }
        for (String eTag : eTags) {
            Arrays.stream(eTag.split(",")).forEach(
                    (item) -> tags.add(delegate.fromString(StringUtils.trim(item))));
        }

        return tags;
    }

    private static Response.ResponseBuilder ifUnmodifiedSince(String strDate, Date lastModified) {
        final Date date = io.esastack.restlight.server.util.DateUtils.parseByCache(strDate);

        if (!date.before(lastModified)) {
            return null;
        }

        return Response.status(PRECONDITION_FAILED.code()).lastModified(lastModified);
    }

    private static Response.ResponseBuilder ifModifiedSince(String strDate, Date lastModified) {
        final Date date = io.esastack.restlight.server.util.DateUtils.parseByCache(strDate);

        if (!date.before(lastModified)) {
            return Response.notModified();
        }

        return null;
    }

    private static String createVaryHeader(List<Variant> variants) {
        boolean acceptLanguage = false;
        boolean accept = false;
        boolean acceptEncoding = false;
        for (Variant variant : variants) {
            if (variant.getLanguage() != null) {
                acceptLanguage = true;
            }
            if (variant.getMediaType() != null) {
                accept = true;
            }
            if (variant.getEncoding() != null) {
                acceptEncoding = true;
            }
        }

        final StringBuilder builder = new StringBuilder();
        boolean emptyBuilder = true;

        if (accept) {
            builder.append(HttpHeaderNames.ACCEPT);
            emptyBuilder = false;
        }
        if (acceptLanguage) {
            if (!emptyBuilder) {
                builder.append(",");
            }
            builder.append(HttpHeaderNames.ACCEPT_LANGUAGE);
            emptyBuilder = false;
        }

        if (acceptEncoding) {
            if (!emptyBuilder) {
                builder.append(",");
            }
            builder.append(HttpHeaderNames.ACCEPT_ENCODING);
        }

        return builder.toString();
    }

    private static boolean isAcceptEncodingMatched(List<String> acceptEncodings, String target) {
        if (acceptEncodings.isEmpty()) {
            return true;
        }

        for (String encoding : acceptEncodings) {
            if ("*".equals(encoding) || encoding.equals(target)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isAcceptLanguagesMatched(List<Locale> acceptLanguages, Locale target) {
        if (acceptLanguages.isEmpty()) {
            return true;
        }

        for (Locale locale : acceptLanguages) {
            String language = locale.getLanguage();
            if ("*".equals(language) || language.equalsIgnoreCase(target.getLanguage())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isMediaTypeMatched(List<MediaType> accepts,
                                              jakarta.ws.rs.core.MediaType target) {
        if (accepts.isEmpty()) {
            return true;
        }

        MediaType converted = MediaTypeUtils.convert(target);
        for (MediaType accept : accepts) {
            if (accept.isCompatibleWith(converted)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This {@link VariantMatcher} is designed to match {@link Variant}s.
     */
    private static class VariantMatcher {

        private static final VariantMatcher INSTANCE = new VariantMatcher();

        /**
         * Selects a {@link Variant} which is best match the {@code request} from the given {@code variants}.
         *
         * @param request  request
         * @param variants variants
         * @return variant
         */
        private Variant match(HttpRequest request, List<Variant> variants) {
            List<String> acceptEncodings = HttpHeaderUtils.getAcceptEncodings(request.headers());
            List<Locale> acceptLanguages = HttpHeaderUtils.getAcceptLanguages(request.headers());
            List<MediaType> accepts = request.accepts();

            List<Variant> matched = new LinkedList<>();
            for (Variant variant : variants) {
                if (isAcceptEncodingMatched(acceptEncodings, variant.getEncoding())
                        && isAcceptLanguagesMatched(acceptLanguages, variant.getLanguage())
                        && isMediaTypeMatched(accepts, variant.getMediaType())) {
                    matched.add(variant);
                }
            }

            if (matched.isEmpty()) {
                return null;
            }

            matched.sort(VariantComparator.INSTANCE);
            return matched.get(0);
        }
    }

    private static class VariantComparator implements Comparator<Variant> {

        private static final VariantComparator INSTANCE = new VariantComparator();

        @Override
        public int compare(Variant o1, Variant o2) {
            MediaType converted1 = MediaTypeUtils.convert(o1.getMediaType());
            MediaType converted2 = MediaTypeUtils.convert(o2.getMediaType());

            int value = MediaTypeUtil.SPECIFICITY_COMPARATOR.compare(converted1, converted2);
            if (value != 0) {
                return value;
            }

            value = compareLanguage(o1.getLanguage(), o2.getLanguage());
            if (value != 0) {
                return value;
            }

            return compareEncoding(o1.getEncoding(), o2.getEncoding());
        }

        private static int compareEncoding(String encoding1, String encoding2) {
            return compareString(encoding1, encoding2);
        }

        private static int compareLanguage(Locale local1, Locale local2) {
            if (local1 == null && local2 == null) {
                return 0;
            } else if (local1 == null) {
                return 1;
            } else if (local2 == null) {
                return -1;
            } else {
                int value = compareString(local1.getLanguage(), local2.getLanguage());
                return value != 0 ? value : compareString(local1.getCountry(), local2.getCountry());
            }
        }

        private static int compareString(String v1, String v2) {
            if (v1 == null && v2 == null) {
                return 0;
            } else if (v1 == null) {
                return 1;
            } else if (v2 == null) {
                return -1;
            } else {
                if (v1.equals("*")) {
                    return 1;
                }
                if (v2.equals("*")) {
                    return -1;
                }
                return 0;
            }
        }
    }

}
