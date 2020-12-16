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
package esa.restlight.core.resolver.arg;

import esa.commons.Checks;
import esa.commons.concurrent.UnsafeUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.commons.reflect.ReflectionUtils;
import esa.httpserver.core.AsyncRequest;
import esa.httpserver.core.AsyncResponse;
import esa.restlight.core.DeployContext;
import esa.restlight.core.annotation.RequestBean;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.method.FieldParam;
import esa.restlight.core.method.FieldParamImpl;
import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.ArgumentResolver;
import esa.restlight.core.resolver.ArgumentResolverFactory;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.util.ConverterUtils;
import esa.restlight.server.util.LoggerUtils;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Binds the parameter if it is annotated by {@link RequestBean}.
 */
public class RequestBeanArgumentResolver implements ArgumentResolverFactory {
    private static final Map<Class<?>, ArgumentResolver> META_CACHE = new ConcurrentHashMap<>(16);
    private final DeployContext<? extends RestlightOptions> ctx;

    public RequestBeanArgumentResolver(DeployContext<? extends RestlightOptions> ctx) {
        Checks.checkNotNull(ctx, "ctx");
        this.ctx = ctx;
    }

    @Override
    public boolean supports(Param param) {
        return param.hasAnnotation(RequestBean.class)
                || (param.isMethodParam() && AnnotationUtils.hasAnnotation(param.methodParam().type(),
                RequestBean.class));
    }

    @Override
    public ArgumentResolver createResolver(Param param,
                                           List<? extends HttpRequestSerializer> serializers) {
        Class<?> type = param.type();
        // instantiate target object by unsafe

        ArgumentResolver resolver = META_CACHE.get(type);
        if (resolver == null) {
            // no need to check the previous value
            META_CACHE.putIfAbsent(type, resolver = new Resolver(newTypeMeta(type,
                    ctx.resolverFactory().orElse(null))));
        }
        return resolver;
    }

    protected TypeMeta newTypeMeta(Class<?> type, HandlerResolverFactory resolverFactory) {
        return new TypeMeta(type, resolverFactory);
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private static final class Resolver implements ArgumentResolver {

        final TypeMeta typeMeta;

        Resolver(TypeMeta typeMeta) {
            this.typeMeta = typeMeta;
        }

        @Override
        public Object resolve(AsyncRequest request, AsyncResponse response) throws Exception {
            final Object allocated = typeMeta.alloc.alloc();

            // set the value to the instance one by one
            for (FieldAndSetter fieldAndSetter : typeMeta.fas) {
                if (fieldAndSetter.resolver != null) {
                    Object resolved = fieldAndSetter.resolver.resolve(request, response);
                    if (resolved != null) {
                        fieldAndSetter.setter.accept(allocated, resolved);
                    }
                }
            }
            return allocated;
        }
    }


    static class TypeMeta {

        private final Allocator alloc;
        final List<FieldAndSetter> fas;

        TypeMeta(Class<?> c, HandlerResolverFactory resolverFactory) {
            this.alloc = detectAllocator(c);
            this.fas = buildFieldAndSetterList(c, resolverFactory);
        }

        private static Allocator detectAllocator(Class<?> c) {
            // priority to use empty constructor to allocate object
            Allocator alloc = ConstructorAllocator.from(c);
            if (alloc == null) {
                // use unsafe to allocate object if empty constructor is missing
                if (UnsafeUtils.hasUnsafe()) {
                    LoggerUtils.logger().debug("Use 'Unsafe' as @QueryBean instance allocator.");
                    alloc = new UnsafeAllocator(c);
                } else {
                    throw new UnsupportedOperationException("Could not initialize instance of " + c.getName() +
                            ", is empty constructor missing?");
                }
            }
            return alloc;
        }

        private List<FieldAndSetter> buildFieldAndSetterList(Class<?> c, HandlerResolverFactory resolverFactory) {
            return ReflectionUtils.getAllDeclaredFields(c)
                    .stream()
                    // exclude static and final field
                    .filter(f -> !Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers()))
                    .map(f -> resolveFieldAndSetter(f, resolverFactory))
                    .collect(Collectors.toList());
        }

        private FieldAndSetter resolveFieldAndSetter(Field f, HandlerResolverFactory resolverFactory) {
            Method method = ReflectionUtils.getSetter(f);
            BiConsumer<Object, Object> setter;
            if (method != null) {
                // use setter to set the field value
                setter = (obj, arg) -> ReflectionUtils.invokeMethod(method, obj, arg);
            } else {
                //use reflection to set the field value
                setter = (obj, arg) -> {
                    f.setAccessible(true);
                    try {
                        f.set(obj, arg);
                    } catch (IllegalAccessException e) {
                        LoggerUtils.logger().warn("Failed to set value to @QueryBean field " + f.toString(), e);
                    }
                };
            }
            FieldParam fieldParam = new FieldParamImpl(f);
            ArgumentResolver resolver = findResolver(fieldParam, resolverFactory);
            return new FieldAndSetter(f, setter, resolver);
        }

        protected ArgumentResolver findResolver(FieldParam fieldParam, HandlerResolverFactory resolverFactory) {
            return resolverFactory.getArgumentResolver(fieldParam);
        }
    }


    private interface Allocator {

        /**
         * Allocate a new object.
         *
         * @return allocated object
         * @throws InstantiationException if error occurred
         */
        Object alloc() throws InstantiationException;

    }

    private static class ConstructorAllocator implements Allocator {

        private final Constructor<?> c;


        private ConstructorAllocator(Constructor<?> c) {
            this.c = c;
            this.c.setAccessible(true);
        }

        static ConstructorAllocator from(Class<?> clazz) {
            ConstructorAllocator alloc = null;
            try {
                alloc = new ConstructorAllocator(clazz.getConstructor());
            } catch (NoSuchMethodException ignored) {
            }
            return alloc;
        }

        @Override
        public Object alloc() throws InstantiationException {
            try {
                return c.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                if (e instanceof InstantiationException) {
                    throw (InstantiationException) e;
                } else {
                    throw new InstantiationException(e.getMessage());
                }
            }
        }
    }

    private static class UnsafeAllocator implements Allocator {


        private final Class<?> c;

        private UnsafeAllocator(Class<?> c) {
            this.c = c;
        }

        @Override
        public Object alloc() throws InstantiationException {
            return UnsafeUtils.getUnsafe().allocateInstance(c);
        }

    }

    /**
     * Hold the Field and setter operation
     */
    private static class FieldAndSetter {
        private final BiConsumer<Object, Object> setter;
        private final ArgumentResolver resolver;

        FieldAndSetter(Field field, BiConsumer<Object, Object> setter,
                       ArgumentResolver resolver) {
            this.resolver = resolver;
            Function<Object, Object> converter = ConverterUtils.converter(field.getGenericType());
            this.setter = (obj, arg) -> setter.accept(obj, converter.apply(arg));
        }
    }
}
