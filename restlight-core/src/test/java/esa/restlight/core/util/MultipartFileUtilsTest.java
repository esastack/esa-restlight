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

import esa.restlight.test.mock.MockAsyncResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartFileUtilsTest {

    @Test
    void testWriteFileToResponse() throws IOException {
        File file = File.createTempFile("restlight-test", ".tmp");
        file.deleteOnExit();
        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write("foo".getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            final MockAsyncResponse response = MockAsyncResponse.aMockResponse().build();
            MultipartFileUtils.writeToResponse(response, file);
            assertEquals(MediaType.MULTIPART_FORM_DATA.value() + ";charset=" + StandardCharsets.UTF_8.name(),
                    response.getHeader(HttpHeaderNames.CONTENT_TYPE));
            assertEquals("attachment" + "; fileName=" + URLEncoder.encode(file.getName(),
                    StandardCharsets.UTF_8.name()),
                    response.getHeader(HttpHeaderNames.CONTENT_DISPOSITION));
            assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), ByteBufUtil.getBytes(response.getSentData()));
        } finally {
            file.delete();
        }
    }

}
