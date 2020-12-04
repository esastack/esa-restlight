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
package esa.restlight.jaxrs.spi.spring;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.util.Constants;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * This is only used in restlight-spring.
 */
@Internal
@Feature(tags = Constants.INTERNAL)
public class PathControllerLocator implements esa.restlight.spring.spi.ControllerLocator {

    @Override
    public Collection<Object> getControllers(ApplicationContext spring,
                                             DeployContext<? extends RestlightOptions> ctx) {
        Map<String, Object> controllers =
                spring.getBeansWithAnnotation(Path.class);
        if (controllers.isEmpty()) {
            controllers = spring.getBeansWithAnnotation(Controller.class);
        }
        if (controllers.isEmpty()) {
            return Collections.emptyList();
        }
        return controllers.values();
    }
}
