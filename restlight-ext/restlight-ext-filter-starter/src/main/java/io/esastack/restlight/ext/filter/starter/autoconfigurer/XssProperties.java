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
package io.esastack.restlight.ext.filter.starter.autoconfigurer;

import io.esastack.restlight.ext.filter.xss.XssOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.esastack.restlight.ext.filter.starter.autoconfigurer.RestlightExtFilterAutoConfiguration.EXT;

@ConfigurationProperties(XssProperties.PREFIX)
public class XssProperties extends XssOptions {

    static final String PREFIX = EXT + "xss";

    private static final long serialVersionUID = -5340429769795888346L;
}
