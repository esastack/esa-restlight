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
package esa.restlight.core.spi;

import esa.commons.spi.SPI;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.MethodAdvice;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @deprecated use {@link HandlerAdviceFactory} please
 */
@SPI
@Deprecated
public interface MethodAdviceFactory {
    /**
     * Creates an optional instance of {@link MethodAdvice} for give target handler.
     *
     * @param ctx    deploy context
     * @param object target object
     * @param method target method
     *
     * @return advice
     */
    Optional<MethodAdvice> methodAdvice(DeployContext<? extends RestlightOptions> ctx,
                                        Object object,
                                        Method method);

}
