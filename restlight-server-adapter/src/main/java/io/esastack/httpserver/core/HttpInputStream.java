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
package io.esastack.httpserver.core;

import java.io.DataInput;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * An {@link InputStream} of http request body.
 */
public abstract class HttpInputStream extends InputStream implements DataInput {

    /**
     * Returns the number of read bytes by this stream so far.
     */
    public abstract int readBytes();

    /**
     * Decodes this readable bytes into a string with the specified character set name.
     */
    public abstract String readString(Charset charset);

}
