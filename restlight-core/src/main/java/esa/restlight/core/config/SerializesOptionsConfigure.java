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

public final class SerializesOptionsConfigure {

    private SerializeOptions request =
            SerializeOptionsConfigure.defaultOpts();
    private SerializeOptions response =
            SerializeOptionsConfigure.defaultOpts();

    private SerializesOptionsConfigure() {
    }

    public static SerializesOptionsConfigure newOpts() {
        return new SerializesOptionsConfigure();
    }

    public static SerializesOptions defaultOpts() {
        return newOpts().configured();
    }

    public SerializesOptionsConfigure request(SerializeOptions request) {
        this.request = request;
        return this;
    }

    public SerializesOptionsConfigure response(SerializeOptions response) {
        this.response = response;
        return this;
    }

    public SerializesOptions configured() {
        SerializesOptions serializesOptions = new SerializesOptions();
        serializesOptions.setRequest(request);
        serializesOptions.setResponse(response);
        return serializesOptions;
    }
}
