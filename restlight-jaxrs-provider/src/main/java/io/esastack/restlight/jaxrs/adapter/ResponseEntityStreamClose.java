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

import esa.commons.collection.AttributeKey;
import esa.commons.io.IOUtils;
import io.esastack.restlight.jaxrs.resolver.ResponseEntityStreamChannelImpl;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpOutputStream;
import io.esastack.restlight.server.core.impl.HttpOutputStreamImpl;

import java.io.OutputStream;

final class ResponseEntityStreamClose {

    private static final AttributeKey<HttpOutputStream> CLOSURE_STREAM = AttributeKey.valueOf("$closure.stream");

    static HttpOutputStreamClosure getNonClosableOutputStream(RequestContext context) {
        HttpOutputStream outputStream = ResponseEntityStreamChannelImpl.get(context).outputStream();
        if (!context.attrs().hasAttr(CLOSURE_STREAM)) {
            context.attrs().attr(CLOSURE_STREAM).set(outputStream);
            context.response().onEnd((rsp) -> close(context));
        }
        return new HttpOutputStreamClosure(outputStream);
    }

    static void close(RequestContext context) {
        HttpOutputStream closable;
        if ((closable = context.attrs().attr(CLOSURE_STREAM).getAndRemove()) != null) {
            IOUtils.closeQuietly(closable);
        }
    }

    private static class HttpOutputStreamClosure extends HttpOutputStreamImpl {

        private HttpOutputStreamClosure(OutputStream underlying) {
            super(underlying);
        }

        @Override
        public void close() {
            // do nothing
            // NOTE: the close should only be invoked when the request has ended.
        }
    }

    private ResponseEntityStreamClose() {
    }

}

