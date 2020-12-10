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
package esa.restlight.ext.multipart.resolver;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.arg.AbstractNameAndValueArgumentResolver;
import esa.restlight.ext.multipart.core.MultipartConfig;
import esa.restlight.ext.multipart.core.MultipartFile;
import esa.restlight.ext.multipart.core.MultipartFileImpl;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractMultipartParamResolver extends AbstractNameAndValueArgumentResolver {

    static final String PREFIX = "$multipart.attr.";
    private static final String MULTIPART_DECODER = "$multipart.decoder";

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractMultipartParamResolver.class);

    private static final String MULTIPART_BODY_RESOLVED = "$multipart.resolved";
    private static final String CLEANER_LISTENER = "$multipart.cleaner";
    static String MULTIPART_FILES = "$multipart.files";

    private final HttpDataFactory factory;

    AbstractMultipartParamResolver(Param param, MultipartConfig config) {
        super(param);
        Checks.checkNotNull(config, "config");
        this.factory = buildFactory(config);
    }

    @Override
    public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
        try {
            return super.resolve(request, response);
        } finally {
            tryAddCleaner(request, response);
        }
    }

    @Override
    protected Object resolveName(String name, AsyncRequest request) throws Exception {
        if (!request.hasAttribute(MULTIPART_BODY_RESOLVED)) {
            final HttpRequest request0 = formattedReq(request);

            if (!HttpPostRequestDecoder.isMultipart(request0)) {
                throw new IllegalStateException("You excepted to accept a multipart file or attribute," +
                        " but Content-Type is: " + request.getHeader(HttpHeaderNames.CONTENT_TYPE));
            }

            final HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(factory, request0);
            // Only decode once and get all resolved data
            List<InterfaceHttpData> resolvedData = decoder.getBodyHttpDatas();
            List<MultipartFile> files = new ArrayList<>(resolvedData.size());
            for (InterfaceHttpData item : resolvedData) {
                InterfaceHttpData.HttpDataType type = item.getHttpDataType();
                if (type == InterfaceHttpData.HttpDataType.Attribute) {
                    request.setAttribute(PREFIX + item.getName(),
                            getAndClean((Attribute) item));
                } else if (type == InterfaceHttpData.HttpDataType.FileUpload) {
                    files.add(parse((FileUpload) item));
                }
            }
            request.setAttribute(MULTIPART_FILES, files);
            request.setAttribute(MULTIPART_BODY_RESOLVED, true);
            request.setAttribute(MULTIPART_DECODER, decoder);
        }

        return getParamValue(name, request);
    }

    private static HttpDataFactory buildFactory(final MultipartConfig config) {
        HttpDataFactory factory;
        if (config.isUseDisk()) {
            factory = new DefaultHttpDataFactory(config.isUseDisk(), config.getCharset());
        } else {
            factory = new DefaultHttpDataFactory(config.getMemoryThreshold(), config.getCharset());
        }
        factory.setMaxLimit(config.getMaxSize());
        final String tempDir = config.getTempDir();
        if (StringUtils.isNotEmpty(tempDir)) {
            DiskFileUpload.baseDirectory = tempDir;
        }
        return factory;
    }

    private static String getAndClean(Attribute attr) throws IOException {
        try {
            return attr.getValue();
        } finally {
            attr.delete();
        }
    }

    private void tryAddCleaner(AsyncRequest request, AsyncResponse response) {
        final List<MultipartFile> files = request.getUncheckedAttribute(MULTIPART_FILES);

        // Note: decoder.destroy() is only allowed to invoke once.
        final HttpPostMultipartRequestDecoder decoder = request.removeUncheckedAttribute(MULTIPART_DECODER);
        if (request.getAttribute(CLEANER_LISTENER) == null && files != null && decoder != null) {
            request.setAttribute(CLEANER_LISTENER, "");
            response.onEnd((r) -> {
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
     * Detect {@link HttpRequest} from known request, try to get it by reflection by default.
     *
     * @param request request
     *
     * @return original http request
     */
    protected HttpRequest formattedReq(AsyncRequest request) {
        return new DefaultFullHttpRequest(request.httpVersion(), request.method(), request.uri(),
                request.byteBufBody(), request.headers(), request.trailers());
    }

    /**
     * Parse the target {@link FileUpload} to {@link MultipartFile}.
     *
     * @param fileUpload source fileUpload
     *
     * @return target multipartFile
     */
    protected MultipartFile parse(FileUpload fileUpload) {
        return new MultipartFileImpl(fileUpload);
    }

    /**
     * Get parameter value from request's attribute.
     *
     * @param name    name
     * @param request request
     *
     * @return obj
     */
    abstract Object getParamValue(String name, AsyncRequest request);
}
