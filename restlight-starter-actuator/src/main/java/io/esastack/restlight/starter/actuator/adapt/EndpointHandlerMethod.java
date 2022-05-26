package io.esastack.restlight.starter.actuator.adapt;

import esa.commons.Checks;
import esa.commons.reflect.ReflectionUtils;
import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.handler.method.HandlerMethodImpl;
import io.esastack.restlight.core.handler.method.MethodParam;
import io.esastack.restlight.core.handler.method.RouteHandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

abstract class EndpointHandlerMethod extends HandlerMethodImpl implements RouteHandlerMethod {
    private static final Method HANDLE_METHOD;
    private final Object bean;
    private final String scheduler;

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

    EndpointHandlerMethod(OperationHandler bean, String scheduler) {
        super(OperationHandler.class, HANDLE_METHOD);
        Checks.checkNotNull(bean, "bean");
        this.bean = bean;
        this.scheduler = scheduler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType, boolean recursive) {
        Annotation methodAnnotation = methodAnnotation();
        if (methodAnnotation != null && annotationType != null
                && annotationType.equals(methodAnnotation.annotationType())) {
            return (A) methodAnnotation;
        }
        return super.getMethodAnnotation(annotationType, recursive);
    }

    @Override
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> annotationType, boolean recursive) {
        Annotation methodAnnotation = methodAnnotation();
        if (methodAnnotation != null && annotationType != null
                && annotationType.equals(methodAnnotation.annotationType())) {
            return true;
        }
        return super.hasMethodAnnotation(annotationType, recursive);
    }

    @Override
    protected MethodParam getMethodParam(int i) {
        MethodParam p = super.getMethodParam(i);
        if (p.index() == 1) {
            return new RequestBodyParam(methodAnnotation(), p);
        }
        return p;
    }

    @Override
    public String scheduler() {
        return scheduler;
    }

    @Override
    public boolean intercepted() {
        return false;
    }

    Object bean() {
        return bean;
    }

    private final class RequestBodyParam implements MethodParam {

        private final Annotation paramAnnotation;
        private final Annotation methodAnnotation;
        private final MethodParam delegate;
        private final Annotation[] annotations;

        private RequestBodyParam(Annotation methodAnnotation, MethodParam delegate) {
            Checks.checkNotNull(delegate, "delegate");
            this.methodAnnotation = methodAnnotation;
            this.paramAnnotation = paramAnnotation();
            this.delegate = delegate;
            if (paramAnnotation != null) {
                Annotation[] anns = delegate.annotations();
                Annotation[] newAnns = new Annotation[anns.length + 1];
                System.arraycopy(delegate.annotations(), 0, newAnns, 0, anns.length);
                newAnns[newAnns.length - 1] = paramAnnotation;
                this.annotations = newAnns;
            } else {
                this.annotations = delegate.annotations();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <A extends Annotation> A getAnnotation(Class<A> ann) {
            if (paramAnnotation != null && ann != null
                    && ann.equals(paramAnnotation.annotationType())) {
                return (A) paramAnnotation;
            }
            return delegate.getAnnotation(ann);
        }

        @Override
        public <A extends Annotation> boolean hasAnnotation(Class<A> ann) {
            if (paramAnnotation != null && ann != null
                    && ann.equals(paramAnnotation.annotationType())) {
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
            if (methodAnnotation != null && ann != null
                    && ann.equals(methodAnnotation.annotationType())) {
                return (A) methodAnnotation;
            }
            return delegate.getMethodAnnotation(ann);
        }

        @Override
        public <A extends Annotation> boolean hasMethodAnnotation(Class<A> ann) {
            if (methodAnnotation != null && ann != null
                    && ann.equals(methodAnnotation.annotationType())) {
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

    abstract Annotation paramAnnotation();

    abstract Annotation methodAnnotation();
}
