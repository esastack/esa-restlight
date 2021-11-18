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
package io.esastack.restlight.ext.multipart.resolver;

import io.esastack.restlight.ext.multipart.annotation.FormParam;
import io.esastack.restlight.ext.multipart.annotation.UploadFile;
import io.esastack.restlight.ext.multipart.core.MultipartFile;

import java.util.List;

public class ResolverSubject {

    public void formParam(@FormParam String foo) {
    }

    public void formParamName(@FormParam("baz") String foo) {
    }

    public void noneRequiredParam(@FormParam(required = false) String foo) {
    }

    public void defaultFormParam(@FormParam(required = false, defaultValue = "foo") String foo) {
    }

    public void defaultAndRequiredFormParam(@FormParam(defaultValue = "foo") String foo) {
    }

    public void formParamCollection(@FormParam(defaultValue = "foo") List<String> foo) {
    }

    public void multipartFile(@UploadFile MultipartFile foo) {
    }

    public void multipartFileName(@UploadFile("multi") MultipartFile foo) {
    }

    public void noneRequiredMultipartFile(@UploadFile(required = false) MultipartFile foo) {
    }

    public void multipartFileList(@UploadFile("foo") List<MultipartFile> files) {
    }

    public void multipartFileAndFormParam0(@UploadFile("foo") List<MultipartFile> file, @FormParam("baz") String name) {
    }

    public void multipartFileAndFormParam1(@FormParam("baz") String name, @UploadFile("foo") List<MultipartFile> file) {
    }
}
