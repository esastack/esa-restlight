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
package io.esastack.restlight.core.resolver.exception;

import esa.commons.Checks;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.resolver.ExceptionResolver;

import java.util.Map;

/**
 * HandlerOnlyExceptionMapper holds all the entries of exception type to {@link ExceptionResolver} in a single handler
 * class(controller bean), and it is only applicable to {@link Handler} who's handler type is same with the {@link
 * #handlerType} of current {@link ExceptionMapper}
 */
public class HandlerOnlyExceptionMapper extends DefaultExceptionMapper {

    /**
     * Type of current controller bean
     */
    private final Class<?> handlerType;

    public HandlerOnlyExceptionMapper(Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings,
                                      Class<?> handlerType) {
        super(mappings);
        Checks.checkNotNull(handlerType, "handlerType");
        this.handlerType = handlerType;
    }

    @Override
    public boolean isApplicable(HandlerMethod handler) {
        return handlerType.equals(handler.beanType());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
