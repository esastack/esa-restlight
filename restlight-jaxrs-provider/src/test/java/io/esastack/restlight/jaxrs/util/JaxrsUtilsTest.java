/*
 * Copyright 2022 OPPO ESA Stack Project
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

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import io.esastack.restlight.core.handler.method.ConstructorParamImpl;
import io.esastack.restlight.core.handler.method.FieldParamImpl;
import io.esastack.restlight.core.handler.method.MethodParamImpl;
import io.esastack.restlight.jaxrs.configure.OrderComponent;
import io.esastack.restlight.jaxrs.impl.core.ResponseImpl;
import io.esastack.restlight.jaxrs.spi.headerdelegate.NewCookieHeaderDelegateFactory;
import io.esastack.restlight.core.context.HttpResponse;
import io.esastack.restlight.core.mock.MockHttpResponse;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.NameBinding;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaxrsUtilsTest {

    @Test
    void testDefaultOrder() {
        assertEquals(Priorities.USER, JaxrsUtils.defaultOrder());
    }

    @Test
    void testGetOrder() {
        assertEquals(JaxrsUtils.defaultOrder(), JaxrsUtils.getOrder(new Object()));
        assertEquals(100, JaxrsUtils.getOrder(new OrderClass1()));
        assertEquals(100, JaxrsUtils.getOrder(OrderClass1.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testToString() {
        assertNull(JaxrsUtils.toString(null));
        final Object obj = new Object();
        assertEquals(obj.toString(), JaxrsUtils.toString(obj));

        final NewCookie cookie = new NewCookie("name", "value");
        assertEquals(((RuntimeDelegate.HeaderDelegate<NewCookie>)
                new NewCookieHeaderDelegateFactory().headerDelegate()).toString(cookie),
                JaxrsUtils.toString(cookie));
    }

    @Test
    void testConvertToMap() {
        final MultivaluedMap<String, Object> map = JaxrsUtils.convertToMap(null);
        assertEquals(0, map.size());

        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("name", "value1");
        headers.add("name", "value2");
        headers.add("name", "value3");
        headers.add("name1", "value1");

        final MultivaluedMap<String, Object> map1 = JaxrsUtils.convertToMap(headers);
        assertEquals(2, map1.keySet().size());
        assertEquals(3, map1.get("name").size());
        assertEquals("value1", map1.get("name").get(0));
        assertEquals("value2", map1.get("name").get(1));
        assertEquals("value3", map1.get("name").get(2));
        assertEquals(1, map1.get("name1").size());
        assertEquals("value1", map1.get("name1").get(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testConvertThenAddToHeaders() {
        final HttpHeaders headers = new Http1HeadersImpl();
        headers.add("n1", "v1");
        headers.add("n1", "v2");
        JaxrsUtils.convertThenAddToHeaders(null, headers);
        assertEquals(2, headers.size());
        assertEquals(2, headers.getAll("n1").size());
        assertEquals("v1", headers.getAll("n1").get(0));
        assertEquals("v2", headers.getAll("n1").get(1));
        JaxrsUtils.convertThenAddToHeaders(new MultivaluedHashMap<>(), headers);
        assertEquals(2, headers.size());
        assertEquals(2, headers.getAll("n1").size());
        assertEquals("v1", headers.getAll("n1").get(0));
        assertEquals("v2", headers.getAll("n1").get(1));

        final MultivaluedMap<String, Object> from = new MultivaluedHashMap<>();
        from.add("n1", "v3");
        final NewCookie c1 = new NewCookie("n", "v");
        final NewCookie c2 = new NewCookie("n1", "v1");
        from.add("n2", c1);
        from.add("n2", c2);
        JaxrsUtils.convertThenAddToHeaders(from, headers);
        assertEquals(3, headers.size());
        assertEquals(1, headers.getAll("n1").size());
        assertEquals("v3", headers.getAll("n1").get(0));
        final RuntimeDelegate.HeaderDelegate<NewCookie> delegate =
                (RuntimeDelegate.HeaderDelegate<NewCookie>) new NewCookieHeaderDelegateFactory().headerDelegate();
        assertEquals(2, headers.getAll("n2").size());
        assertEquals(delegate.toString(c1), headers.getAll("n2").get(0));
        assertEquals(delegate.toString(c2), headers.getAll("n2").get(1));
    }

    @Test
    void testAddMetadataToJakarta() {
        final Object entity1 = new Object();
        final Object entity2 = new Object();
        final HttpResponse from = MockHttpResponse.aMockResponse().build();
        from.status(302);
        from.entity(entity1);
        from.headers().add("n1", "v1");
        from.headers().add("n2", "v2");

        final ResponseImpl to = (ResponseImpl) Response.status(400).entity(entity2).build();
        to.getHeaders().add("n3", "v3");
        to.getHeaders().add("n4", "v4");
        JaxrsUtils.addMetadataToJakarta(from, null);
        assertEquals(302, from.status());
        assertSame(entity1, from.entity());
        assertEquals(2, from.headers().size());
        assertEquals(1, from.headers().getAll("n1").size());
        assertEquals("v1", from.headers().get("n1"));
        assertEquals(1, from.headers().getAll("n2").size());
        assertEquals("v2", from.headers().get("n2"));

        JaxrsUtils.addMetadataToJakarta(null, to);
        assertEquals(400, to.getStatus());
        assertSame(entity2, to.getEntity());
        assertEquals(2, to.getHeaders().size());
        assertEquals(1, to.getHeaders().get("n3").size());
        assertEquals("v3", to.getHeaders().get("n3").get(0));
        assertEquals(1, to.getHeaders().get("n4").size());
        assertEquals("v4", to.getHeaders().get("n4").get(0));

        from.entity(to);
        JaxrsUtils.addMetadataToJakarta(from, to);
        assertEquals(400, to.getStatus());
        assertSame(entity2, to.getEntity());
        assertEquals(4, to.getHeaders().size());
        assertEquals(1, to.getHeaders().get("n1").size());
        assertEquals("v1", to.getHeaders().get("n1").get(0));
        assertEquals(1, to.getHeaders().get("n2").size());
        assertEquals("v2", to.getHeaders().get("n2").get(0));
        assertEquals(1, to.getHeaders().get("n3").size());
        assertEquals("v3", to.getHeaders().get("n3").get(0));
        assertEquals(1, to.getHeaders().get("n4").size());
        assertEquals("v4", to.getHeaders().get("n4").get(0));

        to.getHeaders().remove("n1");
        to.getHeaders().remove("n2");
        from.entity(entity1);
        JaxrsUtils.addMetadataToJakarta(from, to);
        assertEquals(302, to.getStatus());
        assertSame(entity1, to.getEntity());
        assertEquals(4, to.getHeaders().size());
        assertEquals(1, to.getHeaders().get("n1").size());
        assertEquals("v1", to.getHeaders().get("n1").get(0));
        assertEquals(1, to.getHeaders().get("n2").size());
        assertEquals("v2", to.getHeaders().get("n2").get(0));
        assertEquals(1, to.getHeaders().get("n3").size());
        assertEquals("v3", to.getHeaders().get("n3").get(0));
        assertEquals(1, to.getHeaders().get("n4").size());
        assertEquals("v4", to.getHeaders().get("n4").get(0));
    }

    @Test
    void testAddMetadataFromJakarta() {
        final Object entity1 = new Object();
        final Object entity2 = new Object();
        final HttpResponse to = MockHttpResponse.aMockResponse().build();
        to.status(302);
        to.entity(entity1);
        to.headers().add("n1", "v1");
        to.headers().add("n2", "v2");

        final ResponseImpl from = (ResponseImpl) Response.status(400).entity(entity2).build();
        from.getHeaders().add("n3", "v3");
        from.getHeaders().add("n4", "v4");
        JaxrsUtils.addMetadataFromJakarta(null, to);
        assertEquals(302, to.status());
        assertSame(entity1, to.entity());
        assertEquals(2, to.headers().size());
        assertEquals(1, to.headers().getAll("n1").size());
        assertEquals("v1", to.headers().get("n1"));
        assertEquals(1, to.headers().getAll("n2").size());
        assertEquals("v2", to.headers().get("n2"));

        JaxrsUtils.addMetadataFromJakarta(from, null);
        assertEquals(400, from.getStatus());
        assertSame(entity2, from.getEntity());
        assertEquals(2, from.getHeaders().size());
        assertEquals(1, from.getHeaders().get("n3").size());
        assertEquals("v3", from.getHeaders().get("n3").get(0));
        assertEquals(1, from.getHeaders().get("n4").size());
        assertEquals("v4", from.getHeaders().get("n4").get(0));

        to.entity(from);
        JaxrsUtils.addMetadataFromJakarta(from, to);
        assertEquals(400, to.status());
        assertSame(from, to.entity());
        assertEquals(2, to.headers().size());
        assertEquals(1, to.headers().getAll("n3").size());
        assertEquals("v3", to.headers().getAll("n3").get(0));
        assertEquals(1, to.headers().getAll("n4").size());
        assertEquals("v4", to.headers().getAll("n4").get(0));

        to.headers().remove("n3");
        to.headers().remove("n4");
        to.entity(entity1);
        JaxrsUtils.addMetadataFromJakarta(from, to);
        assertEquals(400, to.status());
        assertSame(entity2, to.entity());
        assertEquals(1, to.headers().getAll("n3").size());
        assertEquals("v3", to.headers().getAll("n3").get(0));
        assertEquals(1, to.headers().getAll("n4").size());
        assertEquals("v4", to.headers().getAll("n4").get(0));
    }

    @Test
    void testEqualsIgnoreValueOrder() {
        final MultivaluedMap<String, Object> m1 = new MultivaluedHashMap<>();
        final MultivaluedMap<String, Object> m2 = new MultivaluedHashMap<>();
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(m1, null));
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(null, m2));
        assertTrue(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));
        m1.add("n1", "v1");
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));
        m2.add("n2", "v2");
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));

        m1.add("n2", "v2");
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));
        m2.add("n1", "v1");
        assertTrue(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));

        m2.add("n3", "v3");
        assertFalse(JaxrsUtils.equalsIgnoreValueOrder(m1, m2));
    }

    @Test
    void testIsServerSide() {
        assertTrue(JaxrsUtils.isServerSide(OrderClass1.class));
        assertFalse(JaxrsUtils.isServerSide(ConstrainedClass1.class));
        assertTrue(JaxrsUtils.isServerSide(ConstrainedClass2.class));
    }

    @Test
    void testIsPreMatched() {
        assertFalse(JaxrsUtils.isPreMatched(new Object()));
        assertTrue(JaxrsUtils.isPreMatched(new ConstrainedClass2()));
        assertFalse(JaxrsUtils.isPreMatched(Object.class));
        assertTrue(JaxrsUtils.isPreMatched(ConstrainedClass2.class));
    }

    @Test
    void testHasAnnotation() throws NoSuchFieldException, NoSuchMethodException {
        final Class<?> userType = AnnotationDemo.class;
        assertFalse(JaxrsUtils.hasAnnotation(null, Context.class));
        assertFalse(JaxrsUtils.hasAnnotation(
                new FieldParamImpl(userType.getDeclaredField("configuration")), null));
        assertTrue(JaxrsUtils.hasAnnotation(new ConstructorParamImpl(userType.getConstructors()[0], 0),
                Context.class));
        assertTrue(JaxrsUtils.hasAnnotation(new MethodParamImpl(userType.getDeclaredMethod("setConfiguration",
                Configuration.class), 0), Context.class));
        assertTrue(JaxrsUtils.hasAnnotation(new MethodParamImpl(userType.getDeclaredMethod("setConfiguration0",
                Configuration.class), 0), Context.class));
        assertFalse(JaxrsUtils.hasAnnotation(new MethodParamImpl(userType.getDeclaredMethod("getConfiguration",
                Configuration.class), 0), Context.class));
    }

    @Test
    void testGetAnnotation() throws NoSuchMethodException, NoSuchFieldException {
        final Class<?> userType = AnnotationDemo.class;
        assertNull(JaxrsUtils.getAnnotation(null, Context.class));
        assertNull(JaxrsUtils.getAnnotation(
                new FieldParamImpl(userType.getDeclaredField("configuration")), null));
        assertNotNull(JaxrsUtils.getAnnotation(new ConstructorParamImpl(userType.getConstructors()[0], 0),
                Context.class));
        assertNotNull(JaxrsUtils.getAnnotation(new MethodParamImpl(userType
                .getDeclaredMethod("setConfiguration", Configuration.class), 0), Context.class));
        assertNotNull(JaxrsUtils.getAnnotation(new MethodParamImpl(userType
                .getDeclaredMethod("setConfiguration0", Configuration.class), 0), Context.class));
        assertNotNull(JaxrsUtils.getAnnotation(new MethodParamImpl(userType
                .getDeclaredMethod("getConfiguration", Configuration.class), 0), Context.class));
        assertNull(JaxrsUtils.getAnnotation(new MethodParamImpl(userType
                .getDeclaredMethod("getConfiguration0", Configuration.class), 0), Context.class));
    }

    @Test
    void testFindNameBindings() throws NoSuchMethodException {
        assertTrue(JaxrsUtils.findNameBindings(null).isEmpty());
        assertTrue(JaxrsUtils.findNameBindings(null, true).isEmpty());

        final Set<Class<? extends Annotation>> annos1 = JaxrsUtils.findNameBindings(NameBindingDemo.class);
        assertEquals(2, annos1.size());

        final Set<Class<? extends Annotation>> annos2 = JaxrsUtils.findNameBindings(NameBindingDemo
                .class.getDeclaredMethod("foo"));
        assertEquals(3, annos2.size());

        final Set<Class<? extends Annotation>> annos3 = JaxrsUtils.findNameBindings(NameBindingDemo
                .class.getDeclaredMethod("foo"), false);
        assertEquals(3, annos3.size());

        final Set<Class<? extends Annotation>> annos4 = JaxrsUtils.findNameBindings(NameBindingDemo
                .class.getDeclaredMethod("foo"), true);
        assertEquals(3, annos4.size());
    }

    @Test
    void testAscendingOrdered() {
        final List<OrderComponent<Object>> components = new LinkedList<>();
        components.add(new OrderComponent<>(OrderDemo1.class, JaxrsUtils.getOrder(OrderDemo1.class)));
        components.add(new OrderComponent<>(OrderDemo2.class, JaxrsUtils.getOrder(OrderDemo2.class)));
        components.add(new OrderComponent<>(OrderDemo3.class, JaxrsUtils.getOrder(OrderDemo3.class)));
        JaxrsUtils.ascendingOrdered(components);
        assertEquals(OrderDemo2.class, components.get(0).underlying());
        assertEquals(OrderDemo1.class, components.get(1).underlying());
        assertEquals(OrderDemo3.class, components.get(2).underlying());
    }

    @Test
    void testDescendingOrder() {
        final List<OrderComponent<Object>> components = new LinkedList<>();
        components.add(new OrderComponent<>(OrderDemo1.class, JaxrsUtils.getOrder(OrderDemo1.class)));
        components.add(new OrderComponent<>(OrderDemo2.class, JaxrsUtils.getOrder(OrderDemo2.class)));
        components.add(new OrderComponent<>(OrderDemo3.class, JaxrsUtils.getOrder(OrderDemo3.class)));
        final List<Object> sorted = JaxrsUtils.descendingOrder(components);
        assertEquals(OrderDemo3.class, sorted.get(0));
        assertEquals(OrderDemo1.class, sorted.get(1));
        assertEquals(OrderDemo2.class, sorted.get(2));
    }

    @Test
    void testExtractContracts() {
        assertTrue(JaxrsUtils.extractContracts(null, 100).isEmpty());
        assertTrue(JaxrsUtils.extractContracts(Object.class, 100).isEmpty());

        final Map<Class<?>, Integer> contracts0 = JaxrsUtils.extractContracts(AbstractProvider.class, 10);
        assertEquals(2, contracts0.size());
        assertEquals(10, contracts0.get(ContainerRequestFilter.class));
        assertEquals(10, contracts0.get(ContainerResponseFilter.class));

        final Map<Class<?>, Integer> contracts1 = JaxrsUtils.extractContracts(MyProvider.class, 10);
        assertEquals(2, contracts1.size());
        assertEquals(10, contracts1.get(ContextResolver.class));
        assertEquals(10, contracts1.get(ExceptionMapper.class));

        final Map<Class<?>, Integer> contracts2 = JaxrsUtils.extractContracts(new ProviderImpl(), 10);
        assertEquals(11, contracts2.size());
        assertEquals(10, contracts2.get(ContainerRequestFilter.class));
        assertEquals(10, contracts2.get(ContainerResponseFilter.class));
        assertEquals(10, contracts2.get(ContextResolver.class));
        assertEquals(10, contracts2.get(ExceptionMapper.class));
        assertEquals(10, contracts2.get(MessageBodyReader.class));
        assertEquals(10, contracts2.get(MessageBodyWriter.class));
        assertEquals(10, contracts2.get(Feature.class));
        assertEquals(10, contracts2.get(DynamicFeature.class));
        assertEquals(10, contracts2.get(ReaderInterceptor.class));
        assertEquals(10, contracts2.get(WriterInterceptor.class));
        assertEquals(10, contracts2.get(ParamConverterProvider.class));
    }

    @Test
    void testIsRootResource() {
        assertFalse(JaxrsUtils.isRootResource(OrderDemo1.class));
        assertTrue(JaxrsUtils.isRootResource(RootDemo.class));
        assertTrue(JaxrsUtils.isRootResource(SubRootDemo.class));
    }

    @Test
    void testIsComponent() {
        assertFalse(JaxrsUtils.isComponent(null));
        assertFalse(JaxrsUtils.isComponent(Object.class));
        assertTrue(JaxrsUtils.isComponent(AbstractProvider.class));
        assertTrue(JaxrsUtils.isComponent(MyProvider.class));
        assertTrue(JaxrsUtils.isComponent(ProviderImpl.class));
    }

    @Test
    void testGetComponents() {
        assertTrue(JaxrsUtils.getComponents(null).isEmpty());
        assertTrue(JaxrsUtils.getComponents(Object.class).isEmpty());

        final List<Class<?>> components0 = JaxrsUtils.getComponents(AbstractProvider.class);
        assertEquals(2, components0.size());

        final List<Class<?>> components1 = JaxrsUtils.getComponents(MyProvider.class);
        assertEquals(2, components1.size());

        final List<Class<?>> components2 = JaxrsUtils.getComponents(ProviderImpl.class);
        assertEquals(11, components2.size());
    }

    @Priority(100)
    private static final class OrderClass1 {

    }

    @ConstrainedTo(RuntimeType.CLIENT)
    private static final class ConstrainedClass1 {

    }

    @PreMatching
    @ConstrainedTo(RuntimeType.SERVER)
    private static final class ConstrainedClass2 {

    }

    private static final class AnnotationDemo {

        @Context
        private Configuration configuration;

        public AnnotationDemo(@Context Configuration configuration) {

        }

        public void sayHello(@Context Configuration configuration) {
        }

        @Context
        public void setConfiguration(Configuration configuration) {
        }

        @Context
        private void setConfiguration0(Configuration configuration) {
        }

        @Context
        public void getConfiguration(Configuration configuration) {
        }

        public void getConfiguration0(Configuration configuration) {
        }
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding1 {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding2 {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    private @interface NameBinding3 {
    }

    @NameBinding1
    @NameBinding2
    private static final class NameBindingDemo {

        @NameBinding1
        @NameBinding2
        @NameBinding3
        public void foo() {

        }
    }

    @Priority(0)
    private static final class OrderDemo1 {
    }

    @Priority(-100)
    private static final class OrderDemo2 {
    }

    @Priority(100)
    private static final class OrderDemo3 {
    }

    @Path("abc")
    private static class RootDemo {

    }

    private static final class SubRootDemo extends RootDemo {

    }

    private abstract static class AbstractProvider implements ContainerRequestFilter, ContainerResponseFilter {

    }

    private interface MyProvider extends ContextResolver<String>, ExceptionMapper<RuntimeException> {

    }

    private static final class ProviderImpl extends AbstractProvider implements MyProvider, MessageBodyReader<String>,
            MessageBodyWriter<String>, Feature, DynamicFeature, ReaderInterceptor, WriterInterceptor,
            ParamConverterProvider {
        @Override
        public void filter(ContainerRequestContext requestContext) {

        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        }

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        }

        @Override
        public boolean configure(FeatureContext context) {
            return false;
        }

        @Override
        public String getContext(Class<?> type) {
            return null;
        }

        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public String readFrom(Class<String> type, Type genericType, Annotation[] annotations,
                               MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws WebApplicationException {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {

        }

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            return null;
        }

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws WebApplicationException {
            return null;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws WebApplicationException {

        }
    }
}

