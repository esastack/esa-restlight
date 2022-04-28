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
package io.esastack.restlight.core.resolver.param;

import esa.commons.Checks;
import esa.commons.concurrent.UnsafeUtils;
import esa.commons.reflect.AnnotationUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.annotation.RequestBean;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.FieldParamImpl;
import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.resolver.HandlerResolverFactory;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.core.resolver.ParamResolverFactory;
import io.esastack.restlight.core.resolver.StringConverterProvider;
import io.esastack.restlight.core.serialize.HttpRequestSerializer;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.util.LoggerUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Binds the parameter if it is annotated by {@link RequestBean}.
 */
public class RequestBeanParamResolver implements ParamResolverFactory {

    protected final Map<Class<?>, ParamResolver> metaCache = new ConcurrentHashMap<>(16);
    private final DeployContext ctx;

    public RequestBeanParamResolver(DeployContext ctx) {
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
    public ParamResolver createResolver(Param param,
                                        StringConverterProvider converters,
                                        List<? extends HttpRequestSerializer> serializers) {
        Class<?> type = param.type();
        // instantiate target object by unsafe

        ParamResolver resolver = metaCache.get(type);
        if (resolver == null) {
            // no need to check the previous value
            metaCache.putIfAbsent(type, resolver = new Resolver(newTypeMeta(type,
                    converters,
                    ctx.resolverFactory().orElse(null))));
        }
        return resolver;
    }

    protected TypeMeta newTypeMeta(Class<?> type,
                                   StringConverterProvider converters,
                                   HandlerResolverFactory resolverFactory) {
        return new TypeMeta(type, converters, resolverFactory);
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private static final class Resolver implements ParamResolver {

        final TypeMeta typeMeta;

        private Resolver(TypeMeta typeMeta) {
            this.typeMeta = typeMeta;
        }

        @Override
        public Object resolve(RequestContext context) throws Exception {
            final Object allocated = typeMeta.alloc.alloc();

            // set the value to the instance one by one
            for (FieldAndSetter fieldAndSetter : typeMeta.fas) {
                if (fieldAndSetter.resolver != null) {
                    Object resolved = fieldAndSetter.resolver.resolve(context);
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

        TypeMeta(Class<?> c,
                 StringConverterProvider converters,
                 HandlerResolverFactory resolverFactory) {
            this.alloc = detectAllocator(c);
            this.fas = buildFieldAndSetterList(c, converters, resolverFactory);
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

        private List<FieldAndSetter> buildFieldAndSetterList(Class<?> c,
                                                             StringConverterProvider converters,
                                                             HandlerResolverFactory resolverFactory) {
            return ReflectionUtils.getAllDeclaredFields(c)
                    .stream()
                    // exclude static and final field
                    .filter(f -> !Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers()))
                    .map(f -> resolveFieldAndSetter(f, converters, resolverFactory))
                    .collect(Collectors.toList());
        }

        private FieldAndSetter resolveFieldAndSetter(Field f,
                                                     StringConverterProvider converters,
                                                     HandlerResolverFactory resolverFactory) {
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
                        LoggerUtils.logger().warn("Failed to set value to @QueryBean field " + f, e);
                    }
                };
            }
            FieldParam fieldParam = new FieldParamImpl(f);
            ParamResolver resolver = findResolver(fieldParam, converters, resolverFactory);
            return new FieldAndSetter(setter, resolver);
        }

        protected ParamResolver findResolver(FieldParam fieldParam,
                                             StringConverterProvider converters,
                                             HandlerResolverFactory resolverFactory) {
            return resolverFactory.getParamResolver(fieldParam);
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
        private final ParamResolver resolver;

        private FieldAndSetter(BiConsumer<Object, Object> setter,
                               ParamResolver resolver) {
            this.resolver = resolver;
            this.setter = setter;
        }
    }
}
