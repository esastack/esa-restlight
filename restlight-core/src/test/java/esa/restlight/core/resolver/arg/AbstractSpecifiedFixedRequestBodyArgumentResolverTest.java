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
package esa.restlight.core.resolver.arg;

import esa.restlight.core.annotation.RequestSerializer;
import esa.restlight.core.annotation.Serializer;
import esa.restlight.core.method.MethodParamImpl;
import esa.restlight.core.method.Param;
import esa.restlight.core.serialize.FastJsonHttpBodySerializer;
import esa.restlight.core.serialize.GsonHttpBodySerializer;
import esa.restlight.core.serialize.HttpBodySerializer;
import esa.restlight.core.serialize.ProtoBufHttpBodySerializer;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractSpecifiedFixedRequestBodyArgumentResolverTest {

    @Test
    void testSupports() throws Throwable {
        AbstractSpecifiedFixedRequestBodyArgumentResolver resolver =
                new SpecifiedFixedRequestBodyArgumentResolverImpl();

        assertTrue(resolver.supports(new MethodParamImpl(M1.class.getDeclaredMethod("k1", Object.class),
                0)));
        assertTrue(resolver.supports(new MethodParamImpl(M1.class.getDeclaredMethod("k2", Object.class),
                0)));
        assertTrue(resolver.supports(new MethodParamImpl(M1.class.getDeclaredMethod("k3", Object.class),
                0)));
        assertThrows(IllegalArgumentException.class, () -> resolver.supports(
                new MethodParamImpl(M1.class.getDeclaredMethod("k4", Object.class), 0)));

        assertTrue(resolver.supports(new MethodParamImpl(N1.class.getDeclaredMethod("k1", Object.class),
                0)));
        assertTrue(resolver.supports(new MethodParamImpl(N1.class.getDeclaredMethod("k2", Object.class),
                0)));
        assertTrue(resolver.supports(new MethodParamImpl(N1.class.getDeclaredMethod("k3", Object.class),
                0)));
        assertThrows(IllegalArgumentException.class, () -> resolver.supports(
                new MethodParamImpl(N1.class.getDeclaredMethod("k4", Object.class), 0)));

        assertEquals(100, resolver.getOrder());
    }

    @Test
    void testCreateResolver() throws Throwable {
        AbstractSpecifiedFixedRequestBodyArgumentResolver resolver =
                new SpecifiedFixedRequestBodyArgumentResolverImpl();

        assertTrue(resolver.createResolver(new MethodParamImpl(M1.class.getDeclaredMethod("k1", Object.class),
                0), Collections.singletonList(new ProtoBufHttpBodySerializer()))
                instanceof AbstractSpecifiedFixedRequestBodyArgumentResolver.Resolver);

        assertThrows(IllegalArgumentException.class, () -> resolver.createResolver(
                new MethodParamImpl(M1.class.getDeclaredMethod("k1", Object.class), 0),
                Collections.singletonList(new FastJsonHttpBodySerializer())));

        assertThrows(IllegalArgumentException.class, () -> resolver.createResolver(
                new MethodParamImpl(M1.class.getDeclaredMethod("k1", Object.class), 0),
                Collections.emptyList()));
    }

    @Test
    void testCreateNameAndValue() throws Throwable {
        AbstractSpecifiedFixedRequestBodyArgumentResolver resolver =
                new SpecifiedFixedRequestBodyArgumentResolverImpl();

        final Param param = new MethodParamImpl(M1.class.getDeclaredMethod("k1", Object.class), 0);
        final AbstractSpecifiedFixedRequestBodyArgumentResolver.Resolver resolver0 =
                (AbstractSpecifiedFixedRequestBodyArgumentResolver.Resolver)
                        resolver.createResolver(param, Collections.singletonList(new ProtoBufHttpBodySerializer()));
        final NameAndValue nav0 = resolver.createNameAndValue(param);
        final NameAndValue nav1 = resolver0.createNameAndValue(param);
        assertEquals(nav0.name, nav1.name);
        assertEquals(nav0.required, nav1.required);
        assertEquals(nav0.defaultValue, nav1.defaultValue);
        assertEquals(nav0.hasDefaultValue, nav1.hasDefaultValue);
    }

    @Test
    void testResolveName() throws Throwable {
        AbstractSpecifiedFixedRequestBodyArgumentResolver resolver =
                new SpecifiedFixedRequestBodyArgumentResolverImpl();

        final Param param = new MethodParamImpl(M0.class.getDeclaredMethod("k1", Object.class), 0);
        final AbstractSpecifiedFixedRequestBodyArgumentResolver.Resolver resolver0 =
                (AbstractSpecifiedFixedRequestBodyArgumentResolver.Resolver)
                        resolver.createResolver(param, Collections.singletonList(new HttpBodySerializerImpl()));
        Assertions.assertArrayEquals("Hello!".getBytes(StandardCharsets.UTF_8),
                (byte[]) resolver0.resolveName("xx", MockAsyncRequest.aMockRequest().withBody("xx"
                        .getBytes(StandardCharsets.UTF_8)).build()));
    }

    private static class M0 {
        private void k1(@RequestSerializer(HttpBodySerializerImpl.class) Object obj) {

        }
    }

    private static class HttpBodySerializerImpl extends FastJsonHttpBodySerializer {
        @Override
        public boolean preferStream() {
            return false;
        }

        @Override
        public <T> T deSerialize(byte[] data, Type type) throws Exception {
            return (T) "Hello!".getBytes(StandardCharsets.UTF_8);
        }
    }

    @RequestSerializer(value = ProtoBufHttpBodySerializer.class)
    private static class M1 {

        private void k1(Object obj) {

        }

        private void k2(@RequestSerializer(value = FastJsonHttpBodySerializer.class) Object obj) {

        }

        @RequestSerializer(value = GsonHttpBodySerializer.class)
        private void k3(Object obj) {

        }

        private void k4(@RequestSerializer(value = HttpBodySerializer.class) Object obj) {

        }

    }

    @Serializer(value = ProtoBufHttpBodySerializer.class)
    private static class N1 {

        private void k1(Object obj) {

        }

        private void k2(@Serializer(value = FastJsonHttpBodySerializer.class) Object obj) {

        }

        @Serializer(value = GsonHttpBodySerializer.class)
        private void k3(Object obj) {

        }

        private void k4(@Serializer(value = HttpBodySerializer.class) Object obj) {

        }
    }

    private static class SpecifiedFixedRequestBodyArgumentResolverImpl extends
            AbstractSpecifiedFixedRequestBodyArgumentResolver {

        @Override
        protected NameAndValue createNameAndValue(Param param) {
            return new NameAndValue("xx", false);
        }
    }

}

