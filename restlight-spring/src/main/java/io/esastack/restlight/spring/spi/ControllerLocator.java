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
package io.esastack.restlight.spring.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@SPI
@Internal
public interface ControllerLocator {

    /**
     * Gets the class type of Controller Advice. It means the class annotated by given annotation adviceType is a
     * controller advice class If it returns a class which is assignable from {@link java.lang.annotation.Annotation}.
     *
     * @param spring {@link ApplicationContext} that Restlight is starting with
     * @param ctx    deploy context
     *
     * @return controllers
     */
    default Collection<Object> getControllers(ApplicationContext spring,
                                              DeployContext<? extends RestlightOptions> ctx) {
        Map<String, Object> controllers =
                spring.getBeansWithAnnotation(Controller.class);
        if (controllers.isEmpty()) {
            return Collections.emptyList();
        }
        return controllers.values();
    }

}
