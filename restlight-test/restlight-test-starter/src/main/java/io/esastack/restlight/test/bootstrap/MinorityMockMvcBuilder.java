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
package io.esastack.restlight.test.bootstrap;

import esa.commons.Checks;
import esa.commons.ObjectUtils;
import io.esastack.restlight.core.AbstractRestlight;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.interceptor.HandlerInterceptor;
import io.esastack.restlight.core.resolver.param.ParamResolverAdapter;
import io.esastack.restlight.core.resolver.param.ParamResolverAdviceAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdapter;
import io.esastack.restlight.core.resolver.ret.entity.ResponseEntityResolverAdviceAdapter;
import io.esastack.restlight.core.serialize.HttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonHttpBodySerializer;
import io.esastack.restlight.core.serialize.JacksonSerializer;
import io.esastack.restlight.test.context.DefaultMockMvc;
import io.esastack.restlight.test.context.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinorityMockMvcBuilder implements MockMvcBuilder {

    private final Deployments4Test deployments;

    MinorityMockMvcBuilder(Object... controllers) {
        Checks.checkNotNull(controllers, "controllers");
        this.deployments = Restlight4Test.forServer(RestlightOptionsConfigure.defaultOpts())
                .deployments();
        deployments.addSerializers(Collections.singletonList(
                new JacksonHttpBodySerializer(JacksonSerializer.getDefaultMapper())));
        deployments.addControllers(instantiateIfNecessary(controllers));
    }

    public MinorityMockMvcBuilder controllerAdvices(Object... controllerAdvices) {
        if (controllerAdvices != null) {
            deployments.addControllerAdvices(instantiateIfNecessary(controllerAdvices));
        }
        return this;
    }

    public MinorityMockMvcBuilder serializers(List<? extends HttpBodySerializer> serializers) {
        if (serializers == null) {
            return this;
        }
        serializers.forEach(deployments::addSerializer);
        return this;
    }

    public MinorityMockMvcBuilder paramResolvers(List<ParamResolverAdapter> paramResolvers) {
        if (paramResolvers == null) {
            return this;
        }
        paramResolvers.forEach(deployments::addParamResolver);
        return this;
    }

    public MinorityMockMvcBuilder paramResolverAdvices(List<ParamResolverAdviceAdapter> paramResolverAdvices) {
        if (paramResolverAdvices == null) {
            return this;
        }
        paramResolverAdvices.forEach(deployments::addParamResolverAdvice);
        return this;
    }

    public MinorityMockMvcBuilder responseEntityResolvers(List<ResponseEntityResolverAdapter>
                                                                  responseEntityResolvers) {
        if (responseEntityResolvers == null) {
            return this;
        }
        responseEntityResolvers.forEach(deployments::addResponseEntityResolver);
        return this;
    }

    public MinorityMockMvcBuilder responseEntityResolverAdvices(List<ResponseEntityResolverAdviceAdapter>
                                                                        responseEntityResolverAdvices) {
        if (responseEntityResolverAdvices == null) {
            return this;
        }
        responseEntityResolverAdvices.forEach(deployments::addResponseEntityResolverAdvice);
        return this;
    }

    public MinorityMockMvcBuilder interceptors(final List<HandlerInterceptor> interceptors) {
        if (interceptors == null) {
            return this;
        }
        deployments.addHandlerInterceptors(interceptors);
        return this;
    }

    @Override
    public MockMvc build() {
        final AbstractRestlight server = deployments.server();
        server.start();
        return new DefaultMockMvc(((FakeServer) server.unWrap()).handler);
    }

    private List<Object> instantiateIfNecessary(Object[] target) {
        List<Object> instances = new ArrayList<>(target.length);
        for (Object obj : target) {
            instances.add(obj instanceof Class ? ObjectUtils.instantiateBeanIfNecessary(obj) : obj);
        }
        return instances;
    }

}
