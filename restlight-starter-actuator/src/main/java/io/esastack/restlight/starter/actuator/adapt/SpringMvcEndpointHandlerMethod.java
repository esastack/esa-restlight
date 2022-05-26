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
package io.esastack.restlight.starter.actuator.adapt;

import io.esastack.restlight.springmvc.annotation.shaded.RequestBody0;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseBody0;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class SpringMvcEndpointHandlerMethod extends EndpointHandlerMethod {

    private static final Annotation responseBody = responseBody();
    private static final Annotation requestBody = requestBody();

    SpringMvcEndpointHandlerMethod(WebOperation bean, String scheduler) {
        super(new OperationHandler(bean), scheduler);
    }

    @Override
    Annotation methodAnnotation() {
        return responseBody;
    }

    @Override
    Annotation paramAnnotation() {
        return requestBody;
    }

    @Override
    public String toString() {
        return "SpringMVC Endpoint Handler Proxy";
    }

    private static Annotation responseBody() {
        final Class<?>[] ifs = new Class<?>[]{ResponseBody0.shadedClass()};
        return (Annotation) Proxy.newProxyInstance(ResponseBody0.shadedClass().getClassLoader(),
                ifs,
                new ResponseBodyInvocationHandler());
    }

    private static Annotation requestBody() {
        final Class<?>[] ifs = new Class<?>[]{RequestBody0.shadedClass()};
        return (Annotation) Proxy.newProxyInstance(RequestBody0.shadedClass().getClassLoader(),
                ifs,
                new RequestBodyInvocationHandler());
    }

    private abstract static class AbstractAnnotationInvocationHandler implements InvocationHandler {

        private static final String EQUALS = "equals";
        private static final String HASH_CODE = "hashCode";
        private static final String TO_STRING = "toString";
        private static final String ANNOTATION_TYPE = "annotationType";

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (EQUALS.equals(method.getName())) {
                return annotationType().isInstance(args[0]);
            }
            if (HASH_CODE.equals(method.getName())) {
                return hashCode();
            }

            if (TO_STRING.equals(method.getName())) {
                return annotationType().getSimpleName() + "(EndpointProxy)@" + Integer.toHexString(hashCode());
            }

            if (ANNOTATION_TYPE.equals(method.getName())) {
                return annotationType();
            }
            return this.doInvoke(proxy, method, args);
        }

        abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;

        abstract Class<? extends Annotation> annotationType();
    }

    private static class RequestBodyInvocationHandler extends AbstractAnnotationInvocationHandler {
        private static final String REQUIRED = "required";

        @Override
        Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (REQUIRED.equals(method.getName())) {
                // @RequestBody(required = true)
                return Boolean.FALSE;
            }
            throw new IllegalAccessException("Unexpected access of endpoint handler method: "
                    + method.toGenericString());
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return RequestBody0.shadedClass();
        }
    }

    private static class ResponseBodyInvocationHandler extends AbstractAnnotationInvocationHandler {

        @Override
        Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new IllegalAccessException("Unexpected access of endpoint handler method: "
                    + method.toGenericString());
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return ResponseBody0.shadedClass();
        }
    }
}
