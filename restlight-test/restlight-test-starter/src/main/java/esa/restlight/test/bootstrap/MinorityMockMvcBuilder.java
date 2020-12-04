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
package esa.restlight.test.bootstrap;

import esa.commons.Checks;
import esa.commons.ObjectUtils;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.interceptor.HandlerInterceptor;
import esa.restlight.core.resolver.ArgumentResolverAdapter;
import esa.restlight.core.resolver.ArgumentResolverAdviceAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdapter;
import esa.restlight.core.resolver.ReturnValueResolverAdviceAdapter;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.serialize.JacksonSerializer;
import esa.restlight.test.context.DefaultMockMvc;
import esa.restlight.test.context.MockMvc;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinorityMockMvcBuilder implements MockMvcBuilder {

    private final Deployments4SpringMvcTest deployments;

    MinorityMockMvcBuilder(Object... controllers) {
        Checks.checkNotNull(controllers, "Controllers must not be null!");
        this.deployments = Restlight4SpringMvcTest.forServer(RestlightOptionsConfigure.defaultOpts())
                .deployments();
        deployments.validator(Validation.buildDefaultValidatorFactory().getValidator());
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

    public MinorityMockMvcBuilder validator(Validator validator) {
        if (validator != null) {
            deployments.validator(validator);
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

    public MinorityMockMvcBuilder argumentResolvers(List<ArgumentResolverAdapter> argumentResolvers) {
        if (argumentResolvers == null) {
            return this;
        }
        argumentResolvers.forEach(deployments::addArgumentResolver);
        return this;
    }

    public MinorityMockMvcBuilder argumentResolverAdvices(List<ArgumentResolverAdviceAdapter> argumentResolverAdvices) {
        if (argumentResolverAdvices == null) {
            return this;
        }
        argumentResolverAdvices.forEach(deployments::addArgumentResolverAdvice);
        return this;
    }

    public MinorityMockMvcBuilder returnValueResolvers(List<ReturnValueResolverAdapter> returnValueResolvers) {
        if (returnValueResolvers == null) {
            return this;
        }
        returnValueResolvers.forEach(deployments::addReturnValueResolver);
        return this;
    }

    public MinorityMockMvcBuilder returnValueResolverAdvices(List<ReturnValueResolverAdviceAdapter>
                                                                     returnValueResolverAdvices) {
        if (returnValueResolverAdvices == null) {
            return this;
        }
        returnValueResolverAdvices.forEach(deployments::addReturnValueResolverAdvice);
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
        final Restlight4SpringMvcTest server = deployments.server();
        server.start();
        return new DefaultMockMvc(server.deployments().handler());
    }

    private List<Object> instantiateIfNecessary(Object[] target) {
        List<Object> instances = new ArrayList<>(target.length);
        for (Object obj : target) {
            instances.add(obj instanceof Class ? ObjectUtils.instantiateBeanIfNecessary(obj) : obj);
        }
        return instances;
    }

}
