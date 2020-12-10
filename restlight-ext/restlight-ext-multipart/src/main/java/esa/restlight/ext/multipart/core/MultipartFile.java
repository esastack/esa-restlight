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

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A representation of uploaded file which is wrapped from {@link FileUpload}.
 */
public interface MultipartFile {
    /**
     * Return the name of the parameter in the multipart form.
     *
     * @return the name of the parameter (never {@code null} or empty)
     */
    String filedName();

    /**
     * Get the original file name.
     *
     * @return original file name.
     */
    String originalFilename();

    /**
     * Return the content type of the file.
     *
     * @return the content type, or {@code null} if not defined (or no file has been chosen in the multipart form)
     */
    String contentType();

    /**
     * Return whether the uploaded file is empty, that is, either no file has been chosen in the multipart form or the
     * chosen file has no content.
     */
    boolean isEmpty();

    /**
     * Return the size of the file in bytes.
     *
     * @return the size of the file, or 0 if empty
     */
    long size();

    /**
     * Return the contents of the file as an array of bytes.
     *
     * @return the contents of the file as bytes, or an empty byte array if empty
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    byte[] bytes() throws IOException;

    /**
     * Return an InputStream to read the contents of the file from.
     * <p>The user is responsible for closing the returned stream.
     *
     * @return the contents of the file as stream, or an empty stream if empty
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    InputStream inputStream() throws IOException;

    /**
     * The content transfer encoding.
     *
     * @return encoding
     */
    String contentTransferEncoding();

    /**
     * Whether the content is in memory.
     *
     * @return true if in memory, otherwise false
     */
    boolean isInMemory();

    /**
     * Get the file on disk. Note that if the {@link #isInMemory()} is true then an IOException will be thrown.
     *
     * @return file
     * @throws IOException ex
     */
    File file() throws IOException;

    /**
     * Transfer this to destination file.
     *
     * @param dest destination
     * @throws IOException ex
     */
    void transferTo(File dest) throws IOException;

    /**
     * Get file content with default charset of UTF-8.
     *
     * @return file content
     * @throws IOException ex
     */
    String string() throws IOException;

    /**
     * Get file content with specified charset.
     *
     * @param charset charset
     * @return file content in string
     * @throws IOException ex
     */
    String string(Charset charset) throws IOException;

    /**
     * Release the byteBuf and delete the temp file on disk. Note: It's important to release resource, you can get
     * more information from {@link FileUpload#delete()}. In fact, we'll be happy that you call this method manually.
     */
    void delete();
}
