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
package io.esastack.restlight.ext.multipart.core;

import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.nio.charset.Charset;

/**
 * Wrapper config for {@link DefaultHttpDataFactory} which is designed to customize easily. This config is helpful to
 * decide how to save the multipart, the main logic is:
 * <pre>{@code
 * if (useDisk) {
 *     // Save to disk
 * } else if (checkSize && multipart.size > memoryThreshold) {
 *     // Save to disk
 * } else if (checkSize && multipart.size <= memoryThreshold) {
 *     // Save in memory
 * } else {
 *     // Save in memory
 * }}
 * </pre>
 * You can get more information from {@link DefaultHttpDataFactory#createAttribute(HttpRequest, String)} and {@link
 * DefaultHttpDataFactory#createFileUpload(HttpRequest, String, String, String, String, Charset, long)}.
 * <p>
 * Note that: as mentioned above, the {@link #useDisk} and {@link #memoryThreshold} are mutually exclusive and the
 * former has higher order.
 */
public class MultipartConfig {

    /**
     * Save the multipart to disk no matter what size the item is when the value is true.
     */
    private final boolean useDisk;

    /**
     * Default memoryThreshold as 2MB = 2 * 1024 * 1024
     */
    private long memoryThreshold = 2L * 1024L * 1024L;

    /**
     * Default maxSize as -1 which means disable sizeLimit
     */
    private long maxSize = DefaultHttpDataFactory.MAXSIZE;

    /**
     * Default charset as UTF-8
     */
    private Charset charset = HttpConstants.DEFAULT_CHARSET;

    /**
     * The directory of temp file. see {@link DiskFileUpload#baseDirectory}
     */
    private String tempDir;

    /**
     * The multipart item will on disk if {@link #useDisk} is true, otherwise in memory directly.
     */
    public MultipartConfig(boolean useDisk) {
        this.useDisk = useDisk;
    }

    /**
     * If the multipart item's size greater than {@link #memoryThreshold} then save it on disk, else in memory
     * directly.
     */
    public MultipartConfig(long memoryThreshold) {
        this.useDisk = false;
        if (memoryThreshold > 0) {
            this.memoryThreshold = memoryThreshold;
        }
    }

    public boolean isUseDisk() {
        return useDisk;
    }

    public long getMemoryThreshold() {
        return memoryThreshold;
    }

    public void setMemoryThreshold(long memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
