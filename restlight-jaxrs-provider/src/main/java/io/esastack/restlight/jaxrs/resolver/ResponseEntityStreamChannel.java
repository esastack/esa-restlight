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
package io.esastack.restlight.jaxrs.resolver;

import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.core.context.ResponseEntityChannel;
import io.esastack.restlight.core.context.HttpOutputStream;

import java.io.File;

public interface ResponseEntityStreamChannel extends ResponseEntityChannel {

    /**
     * Get output stream of this response. Create a new implementation of HttpOutputStream if it is {@code null}
     * <p>
     * Note: Once you have called this function you should use this output stream to write your response data rather
     * than using other method such as {@link #end(File)} or {@link #end(Buffer)}.
     *
     * @return output stream
     */
    HttpOutputStream outputStream();
}

