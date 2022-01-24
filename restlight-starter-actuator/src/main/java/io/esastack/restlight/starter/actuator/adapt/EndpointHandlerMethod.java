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

import esa.commons.Checks;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.impl.HandlerImpl;
import io.esastack.restlight.core.method.HandlerMethodImpl;
import io.esastack.restlight.core.method.MethodParam;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.springmvc.annotation.shaded.RequestBody0;
import io.esastack.restlight.springmvc.annotation.shaded.ResponseBody0;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;

class EndpointHandlerMethod extends HandlerMethodImpl {

    private static final Method HANDLE_METHOD;

    static {
        try {
            Method m = OperationHandler.class.getDeclaredMethod(
                    "handle", RequestContext.class, Map.class);
            ReflectionUtils.makeMethodAccessible(m);
            HANDLE_METHOD = m;
        } catch (NoSuchMethodException e) {
            throw new Error("Incompatible class of " + OperationHandler.class.getName(), e);
        }
    }

    private final Annotation responseBody = newResponseBody();

    private EndpointHandlerMethod() {
        super(OperationHandler.class, HANDLE_METHOD);
    }

    static Handler forSpringMvc(WebOperation op) {
        return new HandlerImpl(new EndpointHandlerMethod(), new OperationHandler(op));
    }

    static Handler forJaxrs(WebOperation op) {
        return new HandlerImpl(new EndpointHandlerMethod() {
            @Override
            public String toString() {
                return "Jaxrs Endpoint Handler Proxy";
            }
        }, new OperationHandler(op));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        if (ResponseBody0.shadedClass().equals(annotationType)) {
            return (A) responseBody;
        }
        return super.getMethodAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType) {
        if (ResponseBody0.shadedClass().equals(annotationType)) {
            return true;
        }
        return super.hasMethodAnnotation(annotationType);
    }

    @Override
    protected MethodParam getMethodParam(int i) {
        MethodParam p = super.getMethodParam(i);
        if (p.index() == 1) {
            return new RequestBodyParam(responseBody, p);
        }
        return p;
    }

    @Override
    public String toString() {
        return "SpringMvc Endpoint Handler Proxy";
    }

    private static class RequestBodyParam implements MethodParam {

        private final Annotation requestBody = newRequestBody();
        private final Annotation responseBody;
        private final MethodParam delegate;
        private final Annotation[] annotations;

        private RequestBodyParam(Annotation responseBody,
                                 MethodParam delegate) {
            this.responseBody = responseBody;
            Checks.checkNotNull(delegate, "delegate");
            this.delegate = delegate;
            Annotation[] anns = delegate.annotations();
            Annotation[] newAnns = new Annotation[anns.length + 1];
            System.arraycopy(delegate.annotations(), 0, newAnns, 0, anns.length);
            newAnns[newAnns.length - 1] = requestBody;
            this.annotations = newAnns;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> ann) {
            if (RequestBody0.shadedClass().equals(ann)) {
                return (A) requestBody;
            }
            return delegate.getAnnotation(ann);
        }

        @Override
        public <A extends Annotation> boolean hasAnnotation(Class<A> ann) {
            if (RequestBody0.shadedClass().equals(ann)) {
                return true;
            }
            return delegate.hasAnnotation(ann);
        }

        @Override
        public Annotation[] annotations() {
            return annotations;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <A extends Annotation> A getMethodAnnotation(Class<A> ann) {
            if (ResponseBody0.shadedClass().equals(ann)) {
                return (A) responseBody;
            }
            return delegate.getMethodAnnotation(ann);
        }

        @Override
        public <A extends Annotation> boolean hasMethodAnnotation(Class<A> ann) {
            if (ResponseBody0.shadedClass().equals(ann)) {
                return true;
            }
            return delegate.hasMethodAnnotation(ann);
        }


        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public Class<?> declaringClass() {
            return delegate.declaringClass();
        }

        @Override
        public Class<?> type() {
            return delegate.type();
        }

        @Override
        public Type genericType() {
            return delegate.genericType();
        }

        @Override
        public int index() {
            return delegate.index();
        }

        @Override
        public Parameter parameter() {
            return delegate.parameter();
        }

        @Override
        public Method method() {
            return delegate.method();
        }
    }

    private static Annotation newRequestBody() {
        final Class<?>[] ifs = new Class<?>[]{RequestBody0.shadedClass()};
        return (Annotation) Proxy.newProxyInstance(RequestBody0.shadedClass().getClassLoader(),
                ifs,
                new RequestBodyInvocationHandler());
    }

    private static Annotation newResponseBody() {
        final Class<?>[] ifs = new Class<?>[]{ResponseBody0.shadedClass()};
        return (Annotation) Proxy.newProxyInstance(ResponseBody0.shadedClass().getClassLoader(),
                ifs,
                new ResponseBodyInvocationHandler());
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
