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

import java.io.Serializable;

public class SerializesOptions implements Serializable {

    private static final long serialVersionUID = 967844815313145630L;

    private SerializeOptions request =
            SerializeOptionsConfigure.defaultOpts();
    private SerializeOptions response =
            SerializeOptionsConfigure.defaultOpts();

    public SerializeOptions getRequest() {
        return request;
    }

    public void setRequest(SerializeOptions request) {
        this.request = request;
    }

    public SerializeOptions getResponse() {
        return response;
    }

    public void setResponse(SerializeOptions response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "SerializesOptions{" + "request=" + request +
                ", response=" + response +
                '}';
    }
}
