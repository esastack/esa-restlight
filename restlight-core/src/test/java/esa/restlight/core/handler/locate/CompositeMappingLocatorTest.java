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
package esa.restlight.core.handler.locate;

import esa.restlight.server.route.Mapping;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompositeMappingLocatorTest {

    @Test
    void testWrap() {
        assertNull(CompositeMappingLocator.wrapIfNecessary(Collections.emptyList()));
        final MappingLocator l0 = mock(MappingLocator.class);
        assertEquals(l0, CompositeMappingLocator.wrapIfNecessary(Collections.singleton(l0)));

        final MappingLocator l1 = mock(MappingLocator.class);
        final MappingLocator l2 = mock(MappingLocator.class);
        final Mapping m0 = mock(Mapping.class);
        final Mapping m1 = mock(Mapping.class);
        when(l0.getMapping(any(), any())).thenReturn(Optional.of(m0));
        when(l1.getMapping(any(), any())).thenReturn(Optional.of(m1));
        when(l2.getMapping(any(), any())).thenReturn(Optional.empty());

        final MappingLocator wrapped = CompositeMappingLocator.wrapIfNecessary(Arrays.asList(l0, l1, l2));
        assertNotNull(wrapped);
        final Optional<Mapping> r = wrapped.getMapping(null, null);
        assertTrue(r.isPresent());
        assertSame(m0, r.get());

        final MappingLocator wrapped1 = CompositeMappingLocator.wrapIfNecessary(Arrays.asList(l2, l1, l0));
        assertNotNull(wrapped1);
        final Optional<Mapping> r1 = wrapped1.getMapping(null, null);
        assertTrue(r1.isPresent());
        assertSame(m1, r1.get());
    }

}
