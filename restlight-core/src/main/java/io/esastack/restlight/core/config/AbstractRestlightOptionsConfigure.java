/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.config;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractRestlightOptionsConfigure<C extends AbstractRestlightOptionsConfigure<C, O>,
        O extends RestlightOptions> extends AbstractServerOptionsConfigure<C, O> {

    private String contextPath;
    private SerializesOptions serialize
            = SerializesOptionsConfigure.defaultOpts();
    private Map<String, String> ext = new LinkedHashMap<>();

    public C contextPath(String contextPath) {
        this.contextPath = contextPath;
        return self();
    }

    public C serialize(SerializesOptions serialize) {
        this.serialize = serialize;
        return self();
    }

    public C ext(Map<String, String> ext) {
        this.ext = ext;
        return self();
    }

    @Override
    public O configured() {
        O options = super.configured();
        options.setContextPath(contextPath);
        options.setSerialize(serialize);
        options.setExt(ext);
        return options;
    }
}
