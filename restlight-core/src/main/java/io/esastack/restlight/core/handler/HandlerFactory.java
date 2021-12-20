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
package io.esastack.restlight.core.handler;

import io.esastack.restlight.core.method.Param;
import io.esastack.restlight.core.method.ResolvableParamPredicate;
import io.esastack.restlight.core.resolver.ContextResolver;
import io.esastack.restlight.core.resolver.ParamResolver;
import io.esastack.restlight.server.context.RequestContext;

import java.lang.reflect.Method;

/**
 * This factory is used to get a handler instance by given {@link Class}. If the instance is singleton, we just
 * return it. When the handler is prototype(which means per request a instance), we must instantiates it using
 * current {@link RequestContext}.
 */
public interface HandlerFactory {

    /**
     * Just instantiate a object by given {@link RequestContext}.
     *
     * @param clazz     clazz
     * @param context   context
     * @return  instance
     */
    Object instantiate(Class<?> clazz, RequestContext context);

    /**
     * The {@link ParamResolver}s and {@link ContextResolver}s may be different between two {@link Method}s. So the
     * prototype instances created when invoking different methods of the same {@link Class} may also different from
     * each other, because they have different resolvers.
     *
     * @param clazz     clazz
     * @param method    method
     * @param context   context
     * @return          instance
     */
    Object instantiate(Class<?> clazz, Method method, RequestContext context);

    /**
     * Obtains a instance of given {@link Class}.
     *
     * @param clazz class type
     * @param context   current context, if the handler is prototype, just return it without instantiating a new one.
     * @return  instance
     */
    Object getInstance(Class<?> clazz, RequestContext context);

    /**
     * Obtains a instance of given {@link Class} and {@link Method}.
     *
     * The {@link ParamResolver}s and {@link ContextResolver}s may be different between two {@link Method}s. So the
     * prototype instances created when invoking different methods of the same {@link Class} may also different from
     * each other, because they have different resolvers.
     *
     * @param clazz clazz
     * @param method    method
     * @param context   context
     * @return  instance
     */
    Object getInstance(Class<?> clazz, Method method, RequestContext context);

    /**
     * Try to inject the setters or fields which are {@link ResolvableParamPredicate#test(Param)}.
     *
     * @param instance  instance which is tended to be injected.
     * @param context   current context
     */
    void doInit(Object instance, RequestContext context);

    /**
     * Try to inject the setters or fields which are {@link ResolvableParamPredicate#test(Param)}.
     *
     * The {@link ParamResolver}s and {@link ContextResolver}s may be different between two {@link Method}s. So the
     * prototype instances created when invoking different methods of the same {@link Class} may also different from
     * each other, because they have different resolvers.
     *
     * @param instance  instance which is tended to be injected.
     * @param context   current context
     */
    void doInit(Object instance, Method method, RequestContext context);

    /**
     * Just instantiate a object by given {@link RequestContext} and then inject fields and setters for it.
     *
     * @param clazz clazz
     * @param context   context
     * @return  instance
     */
    default Object instantiateThenInit(Class<?> clazz, RequestContext context) {
        Object instance = instantiate(clazz, context);
        if (instance != null) {
            doInit(instance, context);
        }
        return instance;
    }

    default Object instantiateThenInit(Class<?> clazz, Method method, RequestContext context) {
        Object instance = instantiate(clazz, method, context);
        if (instance != null) {
            doInit(instance, method, context);
        }
        return instance;
    }

    /**
     * Obtains a instance of given {@link Class} and then init it.
     *
     * @param clazz class type
     * @param context   current context
     * @return  instance which has been initialized.
     */
    default Object getThenInit(Class<?> clazz, RequestContext context) {
        Object instance = getInstance(clazz, context);
        if (instance != null) {
            doInit(instance, context);
        }
        return instance;
    }

    default Object getThenInit(Class<?> clazz, Method method, RequestContext context) {
        Object instance = getInstance(clazz, method, context);
        if (instance != null) {
            doInit(instance, method, context);
        }
        return instance;
    }

}

