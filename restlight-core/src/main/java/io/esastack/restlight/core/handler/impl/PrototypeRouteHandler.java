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
package io.esastack.restlight.core.handler.impl;

import esa.commons.StringUtils;
import esa.commons.reflect.BeanUtils;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.handler.HandlerAdvicesFactory;
import io.esastack.restlight.core.handler.HandlerInvoker;
import io.esastack.restlight.core.handler.HandlerValueResolver;
import io.esastack.restlight.core.handler.LinkedHandlerInvoker;
import io.esastack.restlight.core.interceptor.InternalInterceptor;
import io.esastack.restlight.core.method.ConstructorParam;
import io.esastack.restlight.core.method.FieldParam;
import io.esastack.restlight.core.method.HandlerMethod;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.core.method.ResolvableParam;
import io.esastack.restlight.server.bootstrap.WebServerException;

import java.util.List;

public class PrototypeRouteHandler extends AbstractRouteHandler {

    private final HandlerAdvicesFactory handlerAdvicesFactory;

    public PrototypeRouteHandler(DeployContext<? extends RestlightOptions> deployContext,
                                 RouteHandlerMethodAdapter handlerMethod,
                                 HandlerValueResolver handlerResolver,
                                 List<InternalInterceptor> interceptors) {
        super(deployContext, handlerMethod, handlerResolver, interceptors);
        assert deployContext.handlerAdvicesFactory().isPresent();
        this.handlerAdvicesFactory = deployContext.handlerAdvicesFactory().get();
    }

    @Override
    protected Object resolveBean(DeployContext<? extends RestlightOptions> deployContext,
                                 HandlerMethod handler,
                                 RequestContext context) {
        final ResolvableParam<ConstructorParam, ResolverWrap>[] consParams = handlerMethod().consParams();
        final Object[] args = new Object[consParams.length];
        for (int i = 0; i < consParams.length; i++) {
            final ResolvableParam<ConstructorParam, ResolverWrap> resolvable = consParams[i];
            final ConstructorParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                //it may return a null value
                try {
                    args[i] = resolvable.resolver().resolve(deployContext, param, context);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
                continue;
            }
            if (args[i] == null) {
                throw WebServerException.badRequest(
                        StringUtils.concat("Could not resolve constructor parameter at index ",
                                String.valueOf(param.index()), " in ",
                                handlerMethod().constructor() +
                                        ": No suitable resolver for argument of type '", param.type().getName(), "'"));
            }
        }

        Object bean;
        try {
            bean = handlerMethod().constructor().newInstance(args);
        } catch (Throwable th) {
            throw WebServerException.badRequest("Could not instantiate class: [" + handler.beanType() + "]",
                    th);
        }

        this.setFields(bean, context);
        this.invokeSetters(bean, context);
        return bean;
    }

    @Override
    protected HandlerInvoker getInvoker(HandlerMethod handlerMethod, Object bean) {
        Handler handler0 = new HandlerImpl(handlerMethod, bean);
        if (handlerAdvicesFactory != null) {
            HandlerAdvice[] handlerAdvices = handlerAdvicesFactory.getHandlerAdvices(handler0);
            if (handlerAdvices != null && handlerAdvices.length > 0) {
                return LinkedHandlerInvoker.immutable(handlerAdvices, handler0);
            }
        }
        return handler0;
    }

    private void setFields(Object bean, RequestContext context) {
        for (ResolvableParam<FieldParam, ResolverWrap> resolvable : handlerMethod().fieldParams()) {
            final FieldParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                //it may return a null value
                try {
                    BeanUtils.setFieldValue(bean, param.name(),
                            resolvable.resolver().resolve(deployContext(), param, context));
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }
    }

    private void invokeSetters(Object bean, RequestContext context) {
        for (ResolvableParam<MethodParam, ResolverWrap> resolvable : handlerMethod().setterParams()) {
            final MethodParam param = resolvable.param();
            //resolve args with resolver
            if (resolvable.resolver() != null) {
                //it may return a null value
                try {
                    Object arg = resolvable.resolver().resolve(deployContext(), param, context);
                    ReflectionUtils.invokeMethod(param.method(), bean, arg);
                } catch (Exception e) {
                    //wrap exception
                    throw WebServerException.wrap(e);
                }
            }
        }
    }
}

