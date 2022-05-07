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
package io.esastack.restlight.springmvc.resolver.exception;

import esa.commons.ClassUtils;
import esa.commons.StringUtils;
import esa.commons.reflect.AnnotationUtils;
import io.esastack.restlight.core.handler.method.HandlerMethod;
import io.esastack.restlight.core.resolver.exception.ExceptionResolver;
import io.esastack.restlight.core.resolver.exception.DefaultExceptionMapper;
import io.esastack.restlight.core.util.Ordered;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ControllerAdviceExceptionMapper holds all the entries of exception type to {@link ExceptionResolver} in a single
 * advice bean (controller advice bean).
 */
public class ControllerAdviceExceptionMapper extends DefaultExceptionMapper {

    private final Class<?> handlerType;
    private final boolean isController;
    private final int order;
    private final Set<String> basePackages;
    private final List<Class<?>> assignableTypes;
    private final List<Class<? extends Annotation>> annotations;
    private final boolean hasSelctor;

    ControllerAdviceExceptionMapper(Map<Class<? extends Throwable>, ExceptionResolver<Throwable>> mappings,
                                    Object bean,
                                    boolean isController,
                                    String[] basePackages,
                                    Class<?>[] basePackageClasses,
                                    List<Class<?>> assignableTypes,
                                    List<Class<? extends Annotation>> annotations) {
        super(mappings);
        this.handlerType = ClassUtils.getUserType(bean);
        this.isController = isController;
        // keep order minus 1 for the highest
        this.order = initOrderFromBean(bean);
        this.basePackages = initBasePackages(basePackages, basePackageClasses);
        this.assignableTypes = assignableTypes;
        this.annotations = annotations;
        this.hasSelctor = hasSelectors();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isApplicable(HandlerMethod handler) {
        Class<?> beanType = handler.beanType();
        // ignore the advice in the same controller because we expect that this handler should be applicable with the
        // advice in its own controller.
        if (isController && this.handlerType.equals(beanType)) {
            return false;
        }

        if (hasSelctor) {
            for (String basePackage : this.basePackages) {
                if (beanType.getName().startsWith(basePackage)) {
                    return true;
                }
            }
            for (Class<?> clazz : this.assignableTypes) {
                if (clazz.isAssignableFrom(beanType)) {
                    return true;
                }
            }
            for (Class<? extends Annotation> annotationClass : this.annotations) {
                if (AnnotationUtils.findAnnotation(beanType, annotationClass) != null) {
                    return true;
                }
            }
            // has selector but none of them matched.
            return false;
        }
        // has no selector
        return true;
    }

    private boolean hasSelectors() {
        return (!this.basePackages.isEmpty() || !this.assignableTypes.isEmpty() || !this.annotations.isEmpty());
    }

    private static int initOrderFromBean(Object bean) {
        int order = Ordered.LOWEST_PRECEDENCE;
        if (bean instanceof Ordered) {
            order = ((Ordered) bean).getOrder();
        }

        if (order == Ordered.HIGHEST_PRECEDENCE) {
            // never use the HIGHEST_PRECEDENCE because we need to keep the HIGHEST_PRECEDENCE value for
            // ExceptionHandler in current controller.
            order++;
        }
        return order;
    }

    private static Set<String> initBasePackages(String[] basePackageNames, Class<?>[] basePackageClasses) {
        Set<String> basePackages = new LinkedHashSet<>();
        if (basePackageNames != null) {
            for (String basePackage : basePackageNames) {
                if (StringUtils.isNotEmpty(basePackage)) {
                    basePackages.add(adaptBasePackage(basePackage));
                }
            }
        }
        if (basePackageClasses != null) {
            for (Class<?> markerClass : basePackageClasses) {
                int lastDotIndex = markerClass.getName().lastIndexOf('.');
                String packageName = (lastDotIndex != -1 ? markerClass.getName().substring(0, lastDotIndex) : "");
                basePackages.add(adaptBasePackage(packageName));
            }
        }
        return basePackages;
    }

    private static String adaptBasePackage(String basePackage) {
        return (basePackage.endsWith(".") ? basePackage : basePackage + ".");
    }
}
