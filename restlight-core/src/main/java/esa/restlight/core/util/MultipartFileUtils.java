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
package esa.restlight.core.util;

import esa.commons.Checks;
import esa.httpserver.core.AsyncResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @deprecated use {@link AsyncResponse#sendFile(File)} please.
 */
@Deprecated
public final class MultipartFileUtils {

    private static final String CONTENT_DISPOSITION = "Content-disposition";
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final String ATTACHMENT = "attachment";

    public static void writeToResponse(AsyncResponse response, File file) throws IOException {
        writeToResponse(response, new FileInputStream(file), file.getName());
    }

    public static void writeToResponse(AsyncResponse response, InputStream ins, String fileName) throws IOException {
        writeToResponse(response, ins, fileName, StandardCharsets.UTF_8);
    }

    public static void writeToResponse(AsyncResponse response, InputStream ins, String fileName, Charset charset)
            throws IOException {
        Checks.checkNotNull(response, "response");
        Checks.checkNotNull(ins, "ins");
        Checks.checkNotNull(charset, "charset");
        Checks.checkNotEmptyArg(fileName, "fileName");

        response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
                MediaType.MULTIPART_FORM_DATA.value() + ";charset=" + charset.name());
        response.setHeader(CONTENT_DISPOSITION, ATTACHMENT + "; fileName=" + URLEncoder.encode(fileName,
                charset.name()));

        copy(ins, response.outputStream(), new byte[response.bufferSize() > 0 ? response.bufferSize() :
                DEFAULT_BUFFER_SIZE]);
    }

    private static void copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        Checks.checkNotNull(in, "in");
        Checks.checkNotNull(out, "out");
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        in.close();
        out.close();
    }

}
