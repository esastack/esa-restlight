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
package io.esastack.restlight.jaxrs.impl.ext;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.handler.HandlerFactory;
import io.esastack.restlight.jaxrs.configure.ProvidersFactory;
import io.esastack.restlight.jaxrs.configure.ProvidersFactoryImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurableImpl;
import io.esastack.restlight.jaxrs.impl.core.ConfigurationImpl;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProvidersImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new ProvidersImpl(null));
        assertDoesNotThrow(() -> new ProvidersImpl(mock(ProvidersFactory.class)));
    }

    @Test
    void testGetMessageBodyReader() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployContext.handlerFactory()).thenReturn(Optional.of(mock(HandlerFactory.class)));
        final Providers providers0 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        assertNull(providers0.getMessageBodyReader(Object.class, null, null, null));
        assertNull(providers0.getMessageBodyReader(String.class, null, null, null));
        assertNull(providers0.getMessageBodyReader(byte[].class, null, null, null));

        final MessageBodyOperator1 reader1 = new MessageBodyOperator1();
        final MessageBodyOperator2 reader2 = new MessageBodyOperator2();
        configurable.register(reader1);
        configurable.register(reader2);

        final Providers providers1 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        // default to MediaType.APPLICATION_OCTET_STREAM_TYPE
        assertNull(providers1.getMessageBodyReader(Object.class, null, null, null));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getMessageBodyReader(String.class, null, null,
                MediaType.APPLICATION_JSON_TYPE).getClass()));

        // mismatched type
        assertNull(providers1.getMessageBodyReader(int.class, null, null,
                MediaType.APPLICATION_JSON_TYPE));

        // mismatched mediaType
        assertNull(providers1.getMessageBodyReader(String.class, null,
                null, MediaType.TEXT_HTML_TYPE));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getMessageBodyReader(String.class, null, null,
                MediaType.WILDCARD_TYPE).getClass()));
    }

    @Test
    void testGetMessageBodyWriter() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployContext.handlerFactory()).thenReturn(Optional.of(mock(HandlerFactory.class)));
        final Providers providers0 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        assertNull(providers0.getMessageBodyWriter(Object.class, null, null, null));
        assertNull(providers0.getMessageBodyWriter(String.class, null, null, null));
        assertNull(providers0.getMessageBodyWriter(byte[].class, null, null, null));

        final MessageBodyOperator1 writer1 = new MessageBodyOperator1();
        final MessageBodyOperator2 writer2 = new MessageBodyOperator2();
        configurable.register(writer1);
        configurable.register(writer2);

        final Providers providers1 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        // default to MediaType.APPLICATION_OCTET_STREAM_TYPE
        assertNull(providers1.getMessageBodyWriter(Object.class, null, null, null));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getMessageBodyWriter(String.class, null, null,
                MediaType.APPLICATION_JSON_TYPE).getClass()));

        // mismatched type
        assertNull(providers1.getMessageBodyWriter(int.class, null, null,
                MediaType.APPLICATION_JSON_TYPE));

        // mismatched mediaType
        assertNull(providers1.getMessageBodyWriter(String.class, null,
                null, MediaType.TEXT_HTML_TYPE));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getMessageBodyWriter(String.class, null, null,
                MediaType.WILDCARD_TYPE).getClass()));
    }

    @Test
    void testGetExceptionMapper() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployContext.handlerFactory()).thenReturn(Optional.of(mock(HandlerFactory.class)));
        final Providers providers0 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        assertNull(providers0.getExceptionMapper(RuntimeException.class));
        assertNull(providers0.getExceptionMapper(IllegalStateException.class));

        final ExceptionMapper1 mapper1 = new ExceptionMapper1();
        final ExceptionMapper2 mapper2 = new ExceptionMapper2();
        configurable.register(mapper1);
        configurable.register(mapper2);

        final Providers providers1 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));
        assertTrue(Proxy.isProxyClass(providers1.getExceptionMapper(RuntimeException.class).getClass()));
        assertTrue(Proxy.isProxyClass(providers1.getExceptionMapper(IllegalStateException.class).getClass()));
        assertNull(providers1.getExceptionMapper(Error.class));
    }

    @Test
    void testGetContextResolver() {
        final ConfigurationImpl configuration = new ConfigurationImpl();
        final ConfigurableImpl configurable = new ConfigurableImpl(configuration);
        final DeployContext deployContext = mock(DeployContext.class);
        when(deployContext.handlerFactory()).thenReturn(Optional.of(mock(HandlerFactory.class)));
        final Providers providers0 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        assertNull(providers0.getContextResolver(Object.class, null));
        assertNull(providers0.getContextResolver(String.class, null));

        final ContextResolver1 resolver1 = new ContextResolver1();
        final ContextResolver2 resolver2 = new ContextResolver2();
        configurable.register(resolver1);
        configurable.register(resolver2);

        final Providers providers1 = new ProvidersImpl(new ProvidersFactoryImpl(deployContext,
                configuration));

        // default to MediaType.APPLICATION_OCTET_STREAM_TYPE
        assertNull(providers1.getContextResolver(Object.class, null));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getContextResolver(String.class,
                MediaType.APPLICATION_JSON_TYPE).getClass()));

        // mismatched type
        assertNull(providers1.getContextResolver(int.class, MediaType.APPLICATION_JSON_TYPE));

        // mismatched mediaType
        assertNull(providers1.getContextResolver(String.class, MediaType.TEXT_HTML_TYPE));

        // ordered by priority
        assertTrue(Proxy.isProxyClass(providers1.getContextResolver(String.class,
                MediaType.WILDCARD_TYPE).getClass()));
    }

    @Test
    void testConsumes() throws NoSuchMethodException {
        assertEquals(0, ProvidersImpl.consumes(null).size());
        final List<MediaType> types0 = ProvidersImpl.consumes(new MediaTypeDemo());
        assertEquals(1, types0.size());
        assertEquals(MediaType.TEXT_HTML_TYPE, types0.get(0));
        final List<MediaType> types1 = ProvidersImpl.consumes(MediaTypeDemo.class.getDeclaredMethod("sayHello"));
        assertEquals(0, types1.size());
    }

    @Test
    void testProduces() throws NoSuchMethodException {
        assertEquals(0, ProvidersImpl.produces(null).size());
        final List<MediaType> types0 = ProvidersImpl.produces(new MediaTypeDemo());
        assertEquals(1, types0.size());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, types0.get(0));
        final List<MediaType> types1 = ProvidersImpl.produces(MediaTypeDemo.class.getDeclaredMethod("sayHello"));
        assertEquals(0, types1.size());
    }

    @Consumes("text/html")
    @Produces("application/json")
    private static final class MediaTypeDemo {

        @Consumes
        @Produces
        public String sayHello() {
            return "";
        }

        public String foo() {
            return "";
        }

    }

    @Produces("application/json")
    @Consumes("application/json")
    @Priority(100)
    private static final class MessageBodyOperator1 implements MessageBodyReader<CharSequence>,
            MessageBodyWriter<CharSequence> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public CharSequence readFrom(Class<CharSequence> type, Type genericType, Annotation[] annotations,
                                     MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream) throws WebApplicationException {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(CharSequence charSequence, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws WebApplicationException {

        }
    }

    @Produces("application/*")
    @Consumes("application/*")
    @Priority(0)
    private static final class MessageBodyOperator2 implements MessageBodyReader<CharSequence>,
            MessageBodyWriter<CharSequence> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public CharSequence readFrom(Class<CharSequence> type, Type genericType, Annotation[] annotations,
                                     MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream) throws WebApplicationException {
            return null;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(CharSequence charSequence, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws WebApplicationException {

        }
    }

    private static final class ExceptionMapper1 implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }

    private static final class ExceptionMapper2 implements ExceptionMapper<IllegalStateException> {
        @Override
        public Response toResponse(IllegalStateException exception) {
            return null;
        }
    }

    @Produces("application/json")
    @Priority(100)
    private static final class ContextResolver1 implements ContextResolver<CharSequence> {
        @Override
        public CharSequence getContext(Class<?> type) {
            return null;
        }
    }

    @Produces("application/*")
    @Priority(0)
    private static final class ContextResolver2 implements ContextResolver<CharSequence> {
        @Override
        public CharSequence getContext(Class<?> type) {
            return null;
        }
    }
}

