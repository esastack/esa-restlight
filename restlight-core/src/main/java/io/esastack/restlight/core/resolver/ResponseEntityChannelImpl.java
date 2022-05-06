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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.context.impl.RequestContextImpl;

import java.io.File;

public class ResponseEntityChannelImpl implements ResponseEntityChannel {

    protected final ResponseContent content;

    public ResponseEntityChannelImpl(RequestContext context) {
        Checks.checkNotNull(context, "context");
        this.content = Checks.checkNotNull(context.attrs().attr(RequestContextImpl.RESPONSE_CONTENT).get(),
                "response");
    }

    @Override
    public void write(byte[] data) {
        content.write(data);
    }

    @Override
    public void write(Buffer buffer) {
        content.write(buffer);
    }

    @Override
    public void end(byte[] data) {
        if (data == null) {
            end();
            return;
        }

        content.end(data);
    }

    @Override
    public void end(Buffer buffer) {
        if (buffer == null) {
            end();
            return;
        }
        content.end(buffer);
    }

    @Override
    public void end(File file) {
        content.end(file);
    }

    @Override
    public void end() {
        content.end();
    }

    @Override
    public boolean isCommitted() {
        return content.isCommitted();
    }

}

