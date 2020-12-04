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
package esa.restlight.springmvc.spi.spring;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Constants;
import esa.restlight.spring.spi.AdviceLocator;
import esa.restlight.springmvc.annotation.shaded.ControllerAdvice0;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Internal
@Feature(tags = Constants.INTERNAL)
public class ControllerAdviceLocator implements AdviceLocator {

    @Override
    public Collection<Object> getAdvices(ApplicationContext spring, DeployContext<?
            extends RestlightOptions> ctx) {
        Map<String, Object> advices =
                spring.getBeansWithAnnotation(ControllerAdvice0.shadedClass());
        if (advices.isEmpty()) {
            return Collections.emptyList();
        }
        return advices.values();
    }
}
