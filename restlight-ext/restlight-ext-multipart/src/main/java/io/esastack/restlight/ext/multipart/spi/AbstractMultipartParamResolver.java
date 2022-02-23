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
package io.esastack.restlight.ext.multipart.spi;

import esa.commons.Checks;
import esa.commons.collection.AttributeKey;
import esa.commons.function.Function3;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.netty.http.Http1HeadersAdaptor;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.ext.multipart.core.MultipartFile;
import io.esastack.restlight.ext.multipart.core.MultipartFileImpl;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractMultipartParamResolver extends NameAndValueResolverFactory {

    static final AttributeKey<String> PREFIX = AttributeKey.stringKey("$multipart.attr.");
    static final AttributeKey<List<MultipartFile>> MULTIPART_FILES = AttributeKey.valueOf("$multipart.files");

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractMultipartParamResolver.class);

    private static final AttributeKey<HttpPostMultipartRequestDecoder> MULTIPART_DECODER =
            AttributeKey.valueOf("$multipart.decoder");
    private static final AttributeKey<Boolean> MULTIPART_BODY_RESOLVED = AttributeKey.valueOf("$multipart.resolved");
    private static final AttributeKey<String> CLEANER_LISTENER = AttributeKey.stringKey("$multipart.cleaner");

    private final HttpDataFactory factory;

    AbstractMultipartParamResolver(HttpDataFactory factory) {
        Checks.checkNotNull(factory, "factory");
        this.factory = factory;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NameAndValueResolver createResolver(Param param,
                                                  Function3<Class<?>, Type, Param, StringConverter> converterFunc) {
        final NameAndValueResolver resolver = doCreateResolver(param, converterFunc);
        Checks.checkNotNull(resolver, "resolver");
        return new NameAndValueResolver() {
            @Override
            public Object resolve(String name, RequestContext ctx) {
                try {
                    fillMultipart(ctx);
                    return resolver.resolve(name, ctx);
                } finally {
                    tryAddCleaner(ctx);
                }
            }

            @Override
            public NameAndValue createNameAndValue(Param param) {
                return resolver.createNameAndValue(param);
            }
        };
    }

    private void fillMultipart(RequestContext ctx) {
        HttpRequest request = ctx.request();
        if (!ctx.attrs().hasAttr(MULTIPART_BODY_RESOLVED)) {
            final io.netty.handler.codec.http.HttpRequest request0 = formattedReq(request);

            if (!HttpPostRequestDecoder.isMultipart(request0)) {
                throw new IllegalStateException("You excepted to accept a multipart file or attribute," +
                        " but Content-Type is: " + request.contentType());
            }

            final HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, request0);
            // Only decode once and get all resolved data
            List<InterfaceHttpData> resolvedData = decoder.getBodyHttpDatas();
            List<MultipartFile> files = new ArrayList<>(resolvedData.size());
            for (InterfaceHttpData item : resolvedData) {
                InterfaceHttpData.HttpDataType type = item.getHttpDataType();
                if (type == InterfaceHttpData.HttpDataType.Attribute) {
                    try {
                        ctx.attrs().attr(AttributeKey.stringKey(PREFIX + item.getName()))
                                .set(getAndClean((Attribute) item));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (type == InterfaceHttpData.HttpDataType.FileUpload) {
                    files.add(parse((FileUpload) item));
                }
            }
            ctx.attrs().attr(MULTIPART_FILES).set(files);
            ctx.attrs().attr(MULTIPART_BODY_RESOLVED).set(true);
            ctx.attrs().attr(MULTIPART_DECODER).set(decoder);
        }
    }

    /**
     * Creates {@link NameAndValueResolver} by given {@code param} and {@code converterFunc}.
     *
     * @param param             param
     * @param converterFunc     converter function
     * @return                  resolver
     */
    protected abstract NameAndValueResolver doCreateResolver(Param param,
                                                             Function3<Class<?>, Type, Param,
                                                                     StringConverter> converterFunc);


    private static String getAndClean(Attribute attr) throws IOException {
        try {
            return attr.getValue();
        } finally {
            attr.delete();
        }
    }

    private void tryAddCleaner(RequestContext ctx) {
        final List<MultipartFile> files = ctx.attrs().attr(MULTIPART_FILES).get();

        // Note: decoder.destroy() is only allowed to invoke once.
        final HttpPostMultipartRequestDecoder decoder = ctx.attrs().attr(MULTIPART_DECODER).getAndRemove();
        if (ctx.attrs().attr(CLEANER_LISTENER).get() == null && files != null && decoder != null) {
            ctx.attrs().attr(CLEANER_LISTENER).set("");
            ctx.response().onEnd((r) -> {
                for (MultipartFile file : files) {
                    try {
                        file.delete();
                    } catch (Throwable th) {
                        logger.error("Failed to clean temporary resource of upload file {}",
                                file.originalFilename(), th);
                    }
                }

                try {
                    decoder.destroy();
                } catch (Throwable th) {
                    logger.error("Failed to destroy multipart decoder!", th);
                }
            });
        }
    }

    /**
     * Detect {@link io.netty.handler.codec.http.HttpRequest} from known request,
     * try to get it by reflection by default.
     *
     * @param request request
     * @return original http request
     */
    protected io.netty.handler.codec.http.HttpRequest formattedReq(HttpRequest request) {
        return new DefaultFullHttpRequest(convertToNetty(request.httpVersion()),
                HttpMethod.valueOf(request.method().name()),
                request.uri(), (ByteBuf) BufferUtil.unwrap(request.body()),
                convertToNetty(request.headers()), convertToNetty(request.trailers()));
    }

    /**
     * Parse the target {@link FileUpload} to {@link MultipartFile}.
     *
     * @param fileUpload source fileUpload
     * @return target multipartFile
     */
    protected MultipartFile parse(FileUpload fileUpload) {
        return new MultipartFileImpl(fileUpload);
    }

    private static HttpVersion convertToNetty(io.esastack.commons.net.http.HttpVersion version) {
        if (version == io.esastack.commons.net.http.HttpVersion.HTTP_1_0) {
            return HttpVersion.HTTP_1_0;
        } else {
            return HttpVersion.HTTP_1_1;
        }
    }

    private static HttpHeaders convertToNetty(io.esastack.commons.net.http.HttpHeaders headers) {
        if (headers instanceof Http1HeadersImpl) {
            return (Http1HeadersImpl) headers;
        }
        if (headers instanceof Http1HeadersAdaptor) {
            return ((Http1HeadersAdaptor) headers).unwrap();
        }
        return new Http1HeadersImpl().add(headers);
    }
}
