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
package io.esastack.restlight.spring.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class SpringContextUtils {

    public static <T> Optional<T> getBean(ApplicationContext context, Class<T> requiredType) {
        T bean = null;
        try {
            bean = context.getBean(requiredType);
        } catch (BeansException ignored) {
        }
        return Optional.ofNullable(bean);
    }

}
