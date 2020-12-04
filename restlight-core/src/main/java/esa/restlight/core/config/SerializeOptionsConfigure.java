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

import esa.restlight.core.util.Constants;

public final class SerializeOptionsConfigure {
    private boolean negotiation;
    private String negotiationParam = Constants.DEFAULT_NEGOTIATION_FORMAT_PARAMETER;

    private SerializeOptionsConfigure() {
    }

    public static SerializeOptionsConfigure newOpts() {
        return new SerializeOptionsConfigure();
    }

    public static SerializeOptions defaultOpts() {
        return newOpts().configured();
    }

    public SerializeOptionsConfigure negotiation(boolean negotiation) {
        this.negotiation = negotiation;
        return this;
    }

    public SerializeOptionsConfigure negotiationParam(String negotiationParam) {
        this.negotiationParam = negotiationParam;
        return this;
    }

    public SerializeOptions configured() {
        SerializeOptions serializeOptions = new SerializeOptions();
        serializeOptions.setNegotiation(negotiation);
        serializeOptions.setNegotiationParam(negotiationParam);
        return serializeOptions;
    }
}
