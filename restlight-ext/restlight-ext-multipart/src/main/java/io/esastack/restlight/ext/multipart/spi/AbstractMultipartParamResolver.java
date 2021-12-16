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
import esa.commons.StringUtils;
import esa.commons.collection.AttributeKey;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import io.esastack.commons.net.buffer.BufferUtil;
import io.esastack.commons.net.netty.http.Http1HeadersAdaptor;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverter;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolver;
import io.esastack.restlight.core.resolver.nav.NameAndValueResolverFactory;
import io.esastack.restlight.ext.multipart.core.MultipartConfig;
import io.esastack.restlight.ext.multipart.core.MultipartFile;
import io.esastack.restlight.ext.multipart.core.MultipartFileImpl;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

abstract class AbstractMultipartParamResolver<T> extends NameAndValueResolverFactory<T> {

    static final AttributeKey<String> PREFIX = AttributeKey.stringKey("$multipart.attr.");
    private static final AttributeKey<HttpPostMultipartRequestDecoder> MULTIPART_DECODER =
            AttributeKey.valueOf("$multipart.decoder");

    private static final String ENCODING_KEY = "multipart.charset";
    private static final String USE_DISK_KEY = "multipart.use-disk";
    private static final String MEM_THRESHOLD_KEY = "multipart.memory-threshold";
    private static final String MAX_SIZE_KEY = "multipart.max-size";
    private static final String TEMP_DIR_KEY = "multipart.temp-dir";

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractMultipartParamResolver.class);

    private static final AttributeKey<Boolean> MULTIPART_BODY_RESOLVED = AttributeKey.valueOf("$multipart.resolved");
    private static final AttributeKey<String> CLEANER_LISTENER = AttributeKey.stringKey("$multipart.cleaner");
    static final AttributeKey<List<MultipartFile>> MULTIPART_FILES = AttributeKey.valueOf("$multipart.files");

    private HttpDataFactory factory;

    @Override
    public Optional<ParamResolverFactory> factoryBean(DeployContext<? extends RestlightOptions> ctx) {
        initFactory(Checks.checkNotNull(buildConfig(ctx), "config"));
        return super.factoryBean(ctx);
    }

    MultipartConfig buildConfig(DeployContext<? extends RestlightOptions> ctx) {
        MultipartConfig config;

        // Try to get useDiskValue from options, if absent then try to get minSizeValue.
        final Optional<String> useDiskValue = ctx.options().extOption(USE_DISK_KEY);
        config = useDiskValue.map(s -> new MultipartConfig(Boolean.parseBoolean(s)))
                .orElseGet(() -> new MultipartConfig(ctx.options()
                        .extOption(MEM_THRESHOLD_KEY)
                        .map(Long::valueOf)
                        .orElse(0L)));

        ctx.options().extOption(MAX_SIZE_KEY).ifPresent((s) -> config.setMaxSize(Long.parseLong(s)));
        ctx.options().extOption(ENCODING_KEY).ifPresent((s) -> config.setCharset(Charset.forName(s)));
        ctx.options().extOption(TEMP_DIR_KEY).ifPresent((s) -> config.setTempDir(StringUtils.trim(s)));
        return config;
    }

    @Override
    protected BiFunction<String, RequestContext, T> initValueProvider(Param param) {
        BiFunction<String, RequestContext, T> valueProvider =
                Checks.checkNotNull(doInitValueProvider(param), "valueProvider");

        return (name, ctx) -> {
            HttpRequest request = ctx.request();
            if (!ctx.hasAttr(MULTIPART_BODY_RESOLVED)) {
                final io.netty.handler.codec.http.HttpRequest request0 = formattedReq(request);

                if (!HttpPostRequestDecoder.isMultipart(request0)) {
                    throw new IllegalStateException("You excepted to accept a multipart file or attribute," +
                            " but Content-Type is: " + request.headers().get(HttpHeaderNames.CONTENT_TYPE));
                }

                final HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, request0);
                // Only decode once and get all resolved data
                List<InterfaceHttpData> resolvedData = decoder.getBodyHttpDatas();
                List<MultipartFile> files = new ArrayList<>(resolvedData.size());
                for (InterfaceHttpData item : resolvedData) {
                    InterfaceHttpData.HttpDataType type = item.getHttpDataType();
                    if (type == InterfaceHttpData.HttpDataType.Attribute) {
                        try {
                            ctx.attr(AttributeKey.stringKey(PREFIX + item.getName()))
                                    .set(getAndClean((Attribute) item));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (type == InterfaceHttpData.HttpDataType.FileUpload) {
                        files.add(parse((FileUpload) item));
                    }
                }
                ctx.attr(MULTIPART_FILES).set(files);
                ctx.attr(MULTIPART_BODY_RESOLVED).set(true);
                ctx.attr(MULTIPART_DECODER).set(decoder);
            }
            return valueProvider.apply(name, ctx);
        };
    }

    protected abstract BiFunction<String, RequestContext, T> doInitValueProvider(Param param);

    @Override
    protected NameAndValueResolver.Converter<T> initConverter(Param param,
                                                              BiFunction<Class<?>,
                                                                      Type,
                                                                      StringConverter> converterLookup) {
        NameAndValueResolver.Converter<T> converter = Checks.checkNotNull(doInitConverter(param, converterLookup),
                "converter");
        return (name, ctx, valueProvider) -> {
            try {
                return converter.convert(name, ctx, valueProvider);
            } finally {
                if (ctx != null) {
                    tryAddCleaner(ctx);
                }
            }
        };
    }

    protected abstract NameAndValueResolver.Converter<T> doInitConverter(Param param,
                                                                         BiFunction<Class<?>,
                                                                                 Type,
                                                                                 StringConverter> converterLookup);

    void initFactory(final MultipartConfig config) {
        if (config.isUseDisk()) {
            this.factory = new DefaultHttpDataFactory(config.isUseDisk(), config.getCharset());
        } else {
            this.factory = new DefaultHttpDataFactory(config.getMemoryThreshold(), config.getCharset());
        }
        this.factory.setMaxLimit(config.getMaxSize());
        final String tempDir = config.getTempDir();
        if (StringUtils.isNotEmpty(tempDir)) {
            DiskFileUpload.baseDirectory = tempDir;
        }
    }

    private static String getAndClean(Attribute attr) throws IOException {
        try {
            return attr.getValue();
        } finally {
            attr.delete();
        }
    }

    private void tryAddCleaner(RequestContext ctx) {
        final List<MultipartFile> files = ctx.attr(MULTIPART_FILES).get();

        // Note: decoder.destroy() is only allowed to invoke once.
        final HttpPostMultipartRequestDecoder decoder = ctx.attr(MULTIPART_DECODER).getAndRemove();
        if (ctx.attr(CLEANER_LISTENER).get() == null && files != null && decoder != null) {
            ctx.attr(CLEANER_LISTENER).set("");
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
