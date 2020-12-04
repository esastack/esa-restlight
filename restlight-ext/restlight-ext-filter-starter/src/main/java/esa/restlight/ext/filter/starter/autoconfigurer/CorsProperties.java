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
package esa.restlight.ext.filter.starter.autoconfigurer;

import esa.restlight.ext.filter.cors.CorsOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static esa.restlight.ext.filter.starter.autoconfigurer.RestlightExtFilterAutoConfiguration.EXT;

@ConfigurationProperties(CorsProperties.PREFIX)
public class CorsProperties {

    static final String PREFIX = EXT + "cors";

    private List<CorsOptions> rules =
            new LinkedList<>(Collections.singleton(new CorsOptions()));

    public List<CorsOptions> getRules() {
        return rules;
    }

    public void setRules(List<CorsOptions> rules) {
        this.rules = rules;
    }
}
