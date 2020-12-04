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

import java.io.Serializable;

public class SerializeOptions implements Serializable {

    private static final long serialVersionUID = 2912024276351038222L;

    private boolean negotiation;

    private String negotiationParam = Constants.DEFAULT_NEGOTIATION_FORMAT_PARAMETER;

    public boolean isNegotiation() {
        return negotiation;
    }

    public void setNegotiation(boolean negotiation) {
        this.negotiation = negotiation;
    }

    public String getNegotiationParam() {
        return negotiationParam;
    }

    public void setNegotiationParam(String negotiationParam) {
        this.negotiationParam = negotiationParam;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SerializeOptions{");
        sb.append("negotiation=").append(negotiation);
        sb.append(", negotiationParam='").append(negotiationParam).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
