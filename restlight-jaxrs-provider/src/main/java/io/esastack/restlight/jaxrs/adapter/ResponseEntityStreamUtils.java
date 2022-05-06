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
package io.esastack.restlight.jaxrs.adapter;

import esa.commons.collection.Attribute;
import esa.commons.collection.AttributeKey;
import esa.commons.io.IOUtils;
import io.esastack.restlight.jaxrs.resolver.ResponseEntityStreamChannelImpl;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpOutputStream;
import io.esastack.restlight.server.core.impl.HttpOutputStreamImpl;

final class ResponseEntityStreamUtils {

    private static final AttributeKey<HttpOutputStream> CLOSABLE_STREAM = AttributeKey.valueOf("$closable.stream");

    static HttpOutputStream getUnClosableOutputStream(RequestContext context) {
        final HttpOutputStream outputStream = ResponseEntityStreamChannelImpl.get(context).outputStream();
        Attribute<HttpOutputStream> attr = context.attrs().attr(CLOSABLE_STREAM);
        if (attr.get() == null) {
            attr.set(outputStream);
            context.onEnd(ResponseEntityStreamUtils::close);
        }
        return new HttpOutputStreamImpl(outputStream) {
            @Override
            public void close() {
                // do nothing
                // NOTE: the close should only be invoked when the request has ended.
            }
        };
    }

    static void close(RequestContext context) {
        HttpOutputStream closable;
        if ((closable = context.attrs().attr(CLOSABLE_STREAM).getAndRemove()) != null) {
            IOUtils.closeQuietly(closable);
        }
    }

    private ResponseEntityStreamUtils() {
    }

}

