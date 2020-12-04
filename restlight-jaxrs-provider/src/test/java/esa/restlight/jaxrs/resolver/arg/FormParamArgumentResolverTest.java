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
package esa.restlight.jaxrs.resolver.arg;

import esa.restlight.core.method.Param;
import esa.restlight.core.resolver.arg.NameAndValue;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormParamArgumentResolverTest {

    @Test
    void testSupport() {
        final Param param = mock(Param.class);
        when(param.hasAnnotation(FormParam.class)).thenReturn(true);
        assertTrue(new FormParamArgumentResolver().supports(param));
        when(param.hasAnnotation(FormParam.class)).thenReturn(false);
        assertFalse(new FormParamArgumentResolver().supports(param));
    }

    @Test
    void testCreateNameAndValue() {
        final Param param = mock(Param.class);
        final FormParam ann = mock(FormParam.class);
        when(param.getAnnotation(FormParam.class)).thenReturn(ann);
        when(ann.value()).thenReturn("foo");
        final NameAndValue nav = new FormParamArgumentResolver().createNameAndValue(param);
        assertEquals("foo", nav.name);
        assertFalse(nav.required);
        assertNull(nav.defaultValue);
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
        final NameAndValue nav = new FormParamArgumentResolver().createNameAndValue(param);
        assertEquals("foo", nav.name);
        assertFalse(nav.required);
        assertEquals("bar", nav.defaultValue);
    }

}
