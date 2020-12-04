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
package esa.restlight.springmvc.resolver.exception;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.restlight.core.handler.impl.HandlerAdapter;
import esa.restlight.core.handler.locate.HandlerLocator;
import esa.restlight.core.resolver.ExceptionResolver;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.resolver.exception.AbstractExceptionResolverFactory;
import esa.restlight.core.resolver.exception.ExceptionMapper;
import esa.restlight.core.resolver.exception.ExecutionExceptionResolver;
import esa.restlight.core.resolver.exception.HandlerOnlyExceptionMapper;
import esa.restlight.springmvc.annotation.shaded.ControllerAdvice0;
import esa.restlight.springmvc.annotation.shaded.ExceptionHandler0;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpringMvcExceptionResolverFactory extends AbstractExceptionResolverFactory {

    public SpringMvcExceptionResolverFactory(List<ExceptionMapper> mappers,
                                             Collection<?> controllerBeans,
                                             Collection<?> adviceBeans,
                                             HandlerLocator locator,
                                             HandlerResolverFactory factory) {
        super(mappers, controllerBeans, adviceBeans, locator, factory);
    }

    @Override
    protected List<ExceptionMapper> createMappersFromController(Object bean,
                                                                HandlerLocator locator,
                                                                HandlerResolverFactory factory) {
        final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings =
                extractMappings(bean, locator, factory);
        return mappings.isEmpty() ? null :
                Collections.singletonList(new HandlerOnlyExceptionMapper(mappings,
                        ClassUtils.getUserType(bean.getClass())));
    }

    @Override
    protected List<ExceptionMapper> createMappersFromControllerAdvice(Object adviceBean,
                                                                      boolean isController,
                                                                      HandlerLocator locator,
                                                                      HandlerResolverFactory factory) {
        Annotation ann =
                AnnotationUtils.findAnyAnnotation(adviceBean.getClass(), ControllerAdvice0.extendedClasses());
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(adviceBean.getClass(),
                    ControllerAdvice0.shadedClass());
        }

        ControllerAdvice0 controllerAdvice = ControllerAdvice0.fromShade(ann);
        if (controllerAdvice == null) {
            return Collections.emptyList();
        }
        final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings =
                extractMappings(adviceBean, locator, factory);
        return mappings.isEmpty() ? null :
                Collections.singletonList(new ControllerAdviceExceptionMapper(mappings,
                        adviceBean,
                        isController,
                        controllerAdvice.basePackages(),
                        controllerAdvice.basePackageClasses(),
                        Arrays.asList(controllerAdvice.assignableTypes()),
                        Arrays.asList(controllerAdvice.annotations())));
    }

    private Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> extractMappings(
            Object adviceBean,
            HandlerLocator locator,
            HandlerResolverFactory factory) {
        final Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings = new LinkedHashMap<>();
        Class<?> beanType = ClassUtils.getUserType(adviceBean.getClass());
        for (Method method : ClassUtils.userDeclaredMethods(beanType,
                SpringMvcExceptionResolverFactory::isExceptionHandlerMethod)) {
            extractExceptionHandlers(adviceBean, locator, factory, method, mappings);
        }
        return mappings;
    }

    private void extractExceptionHandlers(Object bean,
                                          HandlerLocator locator,
                                          HandlerResolverFactory factory,
                                          Method method,
                                          Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings) {
        for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
            locator.getHandler(method, bean).ifPresent(handler -> {
                ExceptionResolver<Throwable> resolver =
                        new ExecutionExceptionResolver(
                                new HandlerAdapter<>(handler, factory));
                addExceptionMapping(mappings, exceptionType, resolver);
            });
        }
    }

    private void addExceptionMapping(Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings,
                                     Class<? extends Throwable> exceptionType,
                                     ExceptionResolver<Throwable> resolver) {
        ExceptionResolver<Throwable> old = mappings.put(exceptionType, resolver);
        if (old != null) {
            throw new IllegalStateException(StringUtils.concat("More than one method are found to handle [",
                    exceptionType.toString(), "], oldMethod is: ", old.toString(), ", and newMethod is: ",
                    resolver.toString()));
        }
    }

    private static boolean isExceptionHandlerMethod(Method method) {
        return AnnotationUtils.findAnnotation(method,
                ExceptionHandler0.shadedClass()) != null;
    }

    @SuppressWarnings({"unchecked"})
    private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
        List<Class<? extends Throwable>> result = getExceptionTypes(method);

        //detect exception type from method's parameters
        if (result.isEmpty()) {
            for (Class<?> paramType : method.getParameterTypes()) {
                if (Throwable.class.isAssignableFrom(paramType)) {
                    result.add((Class<? extends Throwable>) paramType);
                }
            }
        }

        if (result.isEmpty()) {
            throw new IllegalStateException("No exception types mapped to " + method);
        }
        return result;
    }

    private static List<Class<? extends Throwable>> getExceptionTypes(Method method) {
        //detect exception type from annotation
        ExceptionHandler0 annotation
                = ExceptionHandler0.fromShade(AnnotationUtils.findAnnotation(method, ExceptionHandler0.shadedClass()));
        assert annotation != null;
        return new ArrayList<>(Arrays.asList(annotation.value()));
    }
}
