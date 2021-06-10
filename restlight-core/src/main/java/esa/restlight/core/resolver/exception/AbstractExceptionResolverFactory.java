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
package esa.restlight.core.resolver.exception;

import esa.commons.Checks;
import esa.restlight.core.handler.locate.HandlerLocator;
import esa.restlight.core.resolver.HandlerResolverFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractExceptionResolverFactory extends DefaultExceptionResolverFactory
        implements ExceptionResolverFactory {

    public AbstractExceptionResolverFactory(List<ExceptionMapper> mappers,
                                            Collection<?> controllerBeans,
                                            Collection<?> adviceBeans,
                                            HandlerLocator locator,
                                            HandlerResolverFactory factory) {
        super((mappers == null || mappers.isEmpty())
                ? new ArrayList<>(1)
                : new ArrayList<>(mappers));
        Checks.checkNotNull(locator, "locator");
        Checks.checkNotNull(factory, "factory");
        init(controllerBeans, adviceBeans, locator, factory);
    }

    private void init(Collection<?> controllerBeans,
                      Collection<?> adviceBeans,
                      HandlerLocator locator,
                      HandlerResolverFactory factory) {


        Collection<?> advices;
        if ((adviceBeans == null || adviceBeans.isEmpty())) {
            advices = null;
        } else if (controllerBeans == null || controllerBeans.isEmpty()) {
            advices = adviceBeans;
        } else {
            // remove duplicate
            advices = adviceBeans;
        }

        //exception handler in Controller
        initControllerToExceptionHandlerBean(controllerBeans, locator, factory);
        //exception handler in ControllerAdvice
        initControllerAdviceToExceptionHandlerBean(controllerBeans, advices, locator, factory);
    }

    private void initControllerToExceptionHandlerBean(Collection<?> beans,
                                                      HandlerLocator locator,
                                                      HandlerResolverFactory factory) {
        if (beans == null || beans.isEmpty()) {
            return;
        }

        for (Object bean : beans) {
            List<ExceptionMapper> mappersFromController = this.createMappersFromController(bean, locator, factory);
            if (mappersFromController != null && !mappersFromController.isEmpty()) {
                mappers.addAll(mappersFromController);
            }
        }
    }

    private void initControllerAdviceToExceptionHandlerBean(Collection<?> controllerBeans,
                                                            Collection<?> beans,
                                                            HandlerLocator locator,
                                                            HandlerResolverFactory factory) {

        if (beans == null || beans.isEmpty()) {
            return;
        }
        for (Object adviceBean : beans) {
            boolean isController =
                    controllerBeans != null
                            && !controllerBeans.isEmpty()
                            && controllerBeans.stream()
                            .anyMatch(controller -> controller == adviceBean);
            List<ExceptionMapper> mappersFromControllerAdvice = this.createMappersFromControllerAdvice(adviceBean,
                    isController,
                    locator,
                    factory);
            if (mappersFromControllerAdvice != null && !mappersFromControllerAdvice.isEmpty()) {
                mappers.addAll(mappersFromControllerAdvice);
            }
        }
    }

    /**
     * Detects methods which are designed to handle exceptions in target handler.
     *
     * @param bean    handler
     * @param locator locator
     * @param factory factory
     * @return exception mappers
     */
    protected abstract List<ExceptionMapper> createMappersFromController(Object bean,
                                                                         HandlerLocator locator,
                                                                         HandlerResolverFactory factory);

    /**
     * Detect methods which are designed to handle exceptions in target advice bean.
     *
     * @param adviceBean    advice bean
     * @param isController  whether target bean is a controller
     * @param locator       locator
     * @param factory       factory
     * @return exception mappers
     */
    protected abstract List<ExceptionMapper> createMappersFromControllerAdvice(Object adviceBean,
                                                                               boolean isController,
                                                                               HandlerLocator locator,
                                                                               HandlerResolverFactory factory);
}
