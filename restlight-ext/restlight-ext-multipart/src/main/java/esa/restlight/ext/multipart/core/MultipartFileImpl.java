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
package esa.restlight.ext.multipart.core;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public class MultipartFileImpl implements MultipartFile {

    private final FileUpload upload;

    public MultipartFileImpl(FileUpload fileUpload) {
        Objects.requireNonNull(fileUpload, "FileUpload must not be null!");
        this.upload = fileUpload;
    }

    @Override
    public String filedName() {
        return upload.getName();
    }

    @Override
    public String originalFilename() {
        return upload.getFilename();
    }

    @Override
    public String contentType() {
        return upload.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return upload.length() > 0;
    }

    @Override
    public long size() {
        return upload.length();
    }

    @Override
    public byte[] bytes() throws IOException {
        return upload.get();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteBufInputStream(upload.getByteBuf(), false);
    }

    @Override
    public String contentTransferEncoding() {
        return upload.getContentTransferEncoding();
    }

    @Override
    public boolean isInMemory() {
        return upload.isInMemory();
    }

    @Override
    public File file() throws IOException {
        return upload.getFile();
    }

    @Override
    public void transferTo(File dest) throws IOException {
        upload.renameTo(dest);
    }

    @Override
    public String string() throws IOException {
        return upload.getString();
    }

    @Override
    public String string(Charset charset) throws IOException {
        return upload.getString(charset);
    }

    @Override
    public void delete() {
        upload.delete();
    }
}
