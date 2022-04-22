/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.cases.config;

import io.esastack.restlight.server.handler.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenglu
 */
@Configuration
public class FilterConfig {

    @Bean
    public Filter headerFilter() {
        return (context, chain) -> {
            if (context.request().uri().contains("filter")) {
                context.request().headers().add("name", context.request().getParam("name"));
            }
            return chain.doFilter(context);
        };
    }
}