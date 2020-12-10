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
package esa.restlight.core.serialize;

import esa.commons.Checks;

public abstract class HttpJsonBodySerializerAdapter extends BaseHttpBodySerializer {

    private final Serializer serializer;

    public HttpJsonBodySerializerAdapter(Serializer serializer) {
        Checks.checkNotNull(serializer, "serializer");
        this.serializer = serializer;
    }

    @Override
    protected Serializer serializer() {
        return this.serializer;
    }
}
