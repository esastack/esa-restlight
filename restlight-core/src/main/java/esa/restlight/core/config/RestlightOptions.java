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
package esa.restlight.core.config;

import esa.restlight.server.config.ServerOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RestlightOptions extends ServerOptions {

    private static final long serialVersionUID = 832630904487958795L;

    private String contextPath;
    @Deprecated
    private String validationMessageFile;
    private SerializesOptions serialize
            = SerializesOptionsConfigure.defaultOpts();
    private Map<String, String> ext = new LinkedHashMap<>();

    @Deprecated
    public String getValidationMessageFile() {
        return validationMessageFile;
    }

    @Deprecated
    public void setValidationMessageFile(String validationMessageFile) {
        this.validationMessageFile = validationMessageFile;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }


    public SerializesOptions getSerialize() {
        return serialize;
    }

    public void setSerialize(SerializesOptions serialize) {
        this.serialize = serialize;
    }

    public Map<String, String> getExt() {
        return ext;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }

    public Optional<String> extOption(String key) {
        return Optional.ofNullable(ext.get(key));
    }

    public RestlightOptions extOption(String key, String option) {
        this.ext.put(key, option);
        return this;
    }
}
