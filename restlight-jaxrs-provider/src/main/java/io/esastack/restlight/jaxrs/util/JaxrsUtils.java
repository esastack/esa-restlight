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
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.jaxrs.configure.OrderComponent;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.util.LoggerUtils;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.WriterInterceptor;

import javax.annotation.Priority;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static String toString(Object object) {
        if (object == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        RuntimeDelegate.HeaderDelegate<Object> delegate = (RuntimeDelegate.HeaderDelegate<Object>)
                RuntimeDelegate.getInstance().createHeaderDelegate(ClassUtils.getUserType(object));
        if (delegate != null) {
            return delegate.toString(object);
        }
        return object.toString();
    }

    public static MultivaluedMap<String, Object> convertToMap(HttpHeaders headers) {
        MultivaluedMap<String, Object> dest = new MultivaluedHashMap<>();
        if (headers == null) {
            return dest;
        }
        for (String name : headers.names()) {
            dest.addAll(name, new ArrayList<>(headers.getAll(name)));
        }
        return dest;
    }

    public static void convertThenAddToHeaders(MultivaluedMap<String, Object> values, HttpHeaders headers) {
        if (values == null || values.isEmpty()) {
            return;
        }
        headers.clear();
        for (Map.Entry<String, List<Object>> entry : values.entrySet()) {
            List<Object> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            headers.add(entry.getKey(), value.stream().map(JaxrsUtils::toString)
                    .collect(Collectors.toList()));
        }
    }

    public static void addMetadataToJakarta(HttpResponse from, ResponseImpl to) {
        if (from == null || to == null) {
            return;
        }

        // when the from.entity instanceof Response which means the to is generated from from.entity,
        // we should avoid endless loop setting.
        if (!(from.entity() instanceof Response)) {
            to.setEntity(from.entity());
            to.setStatus(from.status());
        }

        for (Map.Entry<String, String> entry : from.headers()) {
            to.getHeaders().add(entry.getKey(), entry.getValue());
        }
    }

    public static void addMetadataFromJakarta(ResponseImpl from, HttpResponse to) {
        if (from == null || to == null) {
            return;
        }
        to.status(from.getStatus());

        // when the from.entity instanceof Response which means the to is generated from from.entity,
        // we should avoid endless loop setting.
        if (!(to.entity() instanceof Response)) {
            to.entity(from.getEntity());
        }

        to.headers().clear();
        MultivaluedMap<String, String> headers = from.getStringHeaders();
        for (String name : headers.keySet()) {
            to.headers().add(name, headers.get(name));
        }
    }

    public static <K, V1, V2> boolean equalsIgnoreValueOrder(MultivaluedMap<K, V1> m1, MultivaluedMap<K, V2> m2) {
        if (m1 == null) {
            return m2 == null;
        }
        if (m2 == null) {
            return false;
        }
        if (m1.keySet().size() != m2.keySet().size()) {
            return false;
        }
        for (Map.Entry<K, List<V1>> e : m1.entrySet()) {
            List<V2> olist2 = m2.get(e.getKey());
            List<V1> olist1 = e.getValue();
            if (olist1 == null) {
                return olist2 == null;
            }
            if (olist2 == null) {
                return false;
            }
            if (olist1.size() != olist2.size()) {
                return false;
            }
            for (V1 v : olist1) {
                if (!olist2.contains(v)) {
                    return false;
                }
            }
        }
        return true;
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

    public static Set<Class<? extends Annotation>> findNameBindings(Method method, boolean alsoUseClass) {
        if (method == null) {
            return Collections.emptySet();
        }
        Set<Class<? extends Annotation>> annotations = new HashSet<>(findNameBindings(method));
        if (alsoUseClass) {
            annotations.addAll(findNameBindings(method.getDeclaringClass()));
        }
        return annotations;
    }

    public static Set<Class<? extends Annotation>> findNameBindings(AnnotatedElement obj) {
        if (obj == null) {
            return Collections.emptySet();
        }

        Set<Class<? extends Annotation>> result = new HashSet<>(obj.getDeclaredAnnotations().length);
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

    private static Map<Class<?>, Integer> extractContracts(Class<?> clazz, int defaultOrder) {
        if (clazz == null) {
            return Collections.emptyMap();
        }

        Map<Class<?>, Integer> values = new HashMap<>();
        List<Class<?>> components = getComponents(clazz);
        components.forEach(component -> values.put(component, defaultOrder));
        return values;
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

        if (depth > 0 && isComponent(clazz) && clazz.getName().startsWith("jakarta.ws.rs")) {
            classes.add(clazz);
        }

        ++depth;
        for (Class<?> interface0 : clazz.getInterfaces()) {
            getComponentsRecursively(interface0, classes, depth);
        }
        getComponentsRecursively(clazz.getSuperclass(), classes, depth);
    }

    private JaxrsUtils() {
    }
}

