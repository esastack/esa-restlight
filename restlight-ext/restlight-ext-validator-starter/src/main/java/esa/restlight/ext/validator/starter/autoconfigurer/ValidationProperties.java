/*
 * Copyright 2021 OPPO ESA Stack Project
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
package esa.restlight.ext.validator.starter.autoconfigurer;

import esa.restlight.ext.validator.core.ValidationOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ValidationProperties.PREFIX)
public class ValidationProperties extends ValidationOptions {

    static final String PREFIX = "restlight.server.ext.validation";

    private static final long serialVersionUID = -8712327584359762445L;
}

