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
package io.esastack.restlight.jaxrs.resolver.param;

import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.resolver.nav.NameAndValue;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormParamResolverTest {

    @Test
    void testSupport() {
        final Param param = mock(Param.class);
        when(param.hasAnnotation(FormParam.class)).thenReturn(true);
        assertTrue(new FormParamResolver().supports(param));
        when(param.hasAnnotation(FormParam.class)).thenReturn(false);
        assertFalse(new FormParamResolver().supports(param));
    }

    @Test
    void testCreateNameAndValue() {
        final Param param = mock(Param.class);
        final FormParam ann = mock(FormParam.class);
        when(param.getAnnotation(FormParam.class)).thenReturn(ann);
        when(ann.value()).thenReturn("foo");
        final NameAndValue<?> nav = new FormParamResolver().createNameAndValue(param);
        assertEquals("foo", nav.name());
        assertFalse(nav.required());
        assertNull(nav.defaultValue());
    }

    @Test
    void testCreateNameAndValueWithDefaultValue() {
        final Param param = mock(Param.class);
        final FormParam ann = mock(FormParam.class);
        when(param.getAnnotation(FormParam.class)).thenReturn(ann);
        when(ann.value()).thenReturn("foo");
        final DefaultValue def = mock(DefaultValue.class);
        when(def.value()).thenReturn("bar");
        when(param.getAnnotation(DefaultValue.class)).thenReturn(def);
        final NameAndValue<?> nav = new FormParamResolver().createNameAndValue(param);
        assertEquals("foo", nav.name());
        assertFalse(nav.required());
        assertEquals("bar", nav.defaultValue().get());
    }

}

