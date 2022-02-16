/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.jaxrs.util;

import esa.commons.ClassUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.jaxrs.configure.OrderComponent;
import io.esastack.restlight.server.util.LoggerUtils;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

import javax.annotation.Priority;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JaxrsUtils {

    private static final boolean HAS_JAVAX_PRIORITY;

    static {
        final String javaxPriorityName = "javax.annotation.Priority";
        HAS_JAVAX_PRIORITY = ClassUtils.hasClass(javaxPriorityName);
        if (!HAS_JAVAX_PRIORITY) {
            LoggerUtils.logger().debug("No javax.annotation.Priority detected.");
        }
    }

    public static int defaultOrder() {
        return Priorities.USER;
    }

    public static int getOrder(Object obj) {
        if (!HAS_JAVAX_PRIORITY) {
            return defaultOrder();
        }
        Class<?> userType = ClassUtils.getUserType(obj);
        userType = userType.isSynthetic() ? userType.getSuperclass() : userType;
        Priority priority = userType.getAnnotation(Priority.class);
        if (priority == null) {
            return defaultOrder();
        }
        return priority.value();
    }

    public static boolean isServerSide(Class<?> clazz) {
        ConstrainedTo constrain = AnnotationUtils.findAnnotation(clazz, ConstrainedTo.class);
        return constrain == null || RuntimeType.SERVER == constrain.value();
    }

    public static boolean isPreMatched(Object obj) {
        return AnnotationUtils.hasAnnotation(ClassUtils.getUserType(obj), PreMatching.class);
    }

    public static boolean hasAnnotation(Param param, Class<? extends Annotation> target) {
        if (param == null || target == null) {
            return false;
        }
        return param.hasAnnotation(target) || isSetterParam(param, target);
    }

    public static <T extends Annotation> T getAnnotation(Param param, Class<T> target) {
        if (param == null || target == null) {
            return null;
        }
        T annotation = param.getAnnotation(target);
        if (annotation != null) {
            return annotation;
        }
        if (param.isMethodParam()) {
            return AnnotationUtils.findAnnotation(param.methodParam().method(), target);
        } else {
            return null;
        }
    }

    public static List<MediaType> consumes(Object obj) {
        Consumes consumes = AnnotationUtils.findAnnotation(ClassUtils.getUserType(obj), Consumes.class);
        return convertToMediaTypes(consumes == null ? new String[0] : consumes.value());
    }

    public static List<MediaType> produces(Object obj) {
        Produces produces = AnnotationUtils.findAnnotation(ClassUtils.getUserType(obj), Produces.class);
        return convertToMediaTypes(produces == null ? new String[0] : produces.value());
    }

    public static List<Class<? extends Annotation>> annotations(Object obj) {
        Class<?> clazz = ClassUtils.getUserType(obj);
        List<Class<? extends Annotation>> annotations = new LinkedList<>();
        for (Annotation declaredAnn : clazz.getDeclaredAnnotations()) {
            annotations.add(declaredAnn.annotationType());
        }
        return annotations;
    }

    public static List<Class<? extends Annotation>> findNameBindings(Method method, boolean alsoUseClass) {
        List<Class<? extends Annotation>> annotations = new LinkedList<>(findNameBindings(method));
        if (alsoUseClass) {
            annotations.addAll(findNameBindings(method.getDeclaringClass()));
        }
        return annotations;
    }

    public static List<Class<? extends Annotation>> findNameBindings(AnnotatedElement obj) {
        if (obj == null) {
            return Collections.emptyList();
        }

        List<Class<? extends Annotation>> result = new ArrayList<>(obj.getDeclaredAnnotations().length);
        for (Annotation declaredAnn : obj.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = declaredAnn.annotationType();
            if (type.getDeclaredAnnotation(NameBinding.class) != null) {
                result.add(type);
            }
        }

        return result;
    }

    public static <T> List<T> ascendingOrdered(List<OrderComponent<T>> components) {
        OrderedComparator.sort(components);
        return components.stream().map(OrderComponent::underlying).collect(Collectors.toList());
    }

    public static <T> List<T> descendingOrder(List<OrderComponent<T>> components) {
        List<T> result = ascendingOrdered(components);
        Collections.reverse(result);
        return result;
    }

    public static Map<Class<?>, Integer> extractContracts(Object obj, int defaultOrder) {
        return extractContracts(ClassUtils.getUserType(obj), defaultOrder);
    }

    public static Map<Class<?>, Integer> extractContracts(Class<?> clazz, int defaultOrder) {
        if (clazz == null) {
            return Collections.emptyMap();
        }

        Map<Class<?>, Integer> values = new HashMap<>();
        List<Class<?>> components = getComponents(clazz);
        components.forEach(component -> values.put(component, defaultOrder));
        return values;
    }

    public static boolean isRootResource(Class<?> clazz) {
        return AnnotationUtils.hasAnnotation(clazz, Path.class, true);
    }

    public static boolean isMessageBodyReader(Class<?> clazz) {
        return MessageBodyReader.class.isAssignableFrom(clazz);
    }

    public static boolean isMessageBodyWriter(Class<?> clazz) {
        return MessageBodyWriter.class.isAssignableFrom(clazz);
    }

    public static boolean isContextResolver(Class<?> clazz) {
        return ContextResolver.class.isAssignableFrom(clazz);
    }

    public static boolean isExceptionMapper(Class<?> clazz) {
        return ExceptionMapper.class.isAssignableFrom(clazz);
    }

    public static boolean isFeature(Class<?> clazz) {
        return Feature.class.isAssignableFrom(clazz);
    }

    public static boolean isDynamicFeature(Class<?> clazz) {
        return DynamicFeature.class.isAssignableFrom(clazz);
    }

    public static boolean isRequestFilter(Class<?> clazz) {
        return ContainerRequestFilter.class.isAssignableFrom(clazz);
    }

    public static boolean isResponseFilter(Class<?> clazz) {
        return ContainerResponseFilter.class.isAssignableFrom(clazz);
    }

    public static boolean isReaderInterceptor(Class<?> clazz) {
        return ReaderInterceptor.class.isAssignableFrom(clazz);
    }

    public static boolean isWriterInterceptor(Class<?> clazz) {
        return WriterInterceptor.class.isAssignableFrom(clazz);
    }

    public static boolean isParamConverterProvider(Class<?> clazz) {
        return ParamConverterProvider.class.isAssignableFrom(clazz);
    }

    public static boolean isComponent(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return JaxrsUtils.isMessageBodyReader(clazz)
                || JaxrsUtils.isMessageBodyWriter(clazz)
                || JaxrsUtils.isContextResolver(clazz)
                || JaxrsUtils.isExceptionMapper(clazz)
                || JaxrsUtils.isFeature(clazz)
                || JaxrsUtils.isDynamicFeature(clazz)
                || JaxrsUtils.isRequestFilter(clazz)
                || JaxrsUtils.isResponseFilter(clazz)
                || JaxrsUtils.isReaderInterceptor(clazz)
                || JaxrsUtils.isWriterInterceptor(clazz)
                || JaxrsUtils.isParamConverterProvider(clazz);
    }

    public static List<Class<?>> getComponents(Class<?> clazz) {
        List<Class<?>> classes = new LinkedList<>();
        getComponentsRecursively(clazz, classes, 0);
        return classes;
    }

    private static boolean isSetterParam(Param param, Class<? extends Annotation> target) {
        if (!param.isMethodParam()) {
            return false;
        }
        MethodParam mParam = param.methodParam();
        if (!ReflectionUtils.isSetter(mParam.method())) {
            return false;
        }
        if (mParam.method().getParameterCount() != 1) {
            return false;
        }
        return AnnotationUtils.hasAnnotation(mParam.method(), target);
    }

    private static void getComponentsRecursively(Class<?> clazz, List<Class<?>> classes, int depth) {
        if (clazz == null || clazz.equals(Object.class)) {
            return;
        }

        if (depth > 0 && isComponent(clazz)) {
            classes.add(clazz);
        }

        ++depth;
        for (Class<?> interface0 : clazz.getInterfaces()) {
            getComponentsRecursively(interface0, classes, depth);
        }
        getComponentsRecursively(clazz.getSuperclass(), classes, depth);
    }

    private static List<MediaType> convertToMediaTypes(String[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }
        List<MediaType> mediaTypes = new ArrayList<>(values.length);
        for (String value : values) {
            mediaTypes.add(MediaType.valueOf(value));
        }
        return mediaTypes;
    }

    private JaxrsUtils() {
    }
}

