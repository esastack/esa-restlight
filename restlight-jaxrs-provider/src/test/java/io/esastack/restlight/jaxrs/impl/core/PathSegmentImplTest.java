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
package io.esastack.restlight.jaxrs.impl.core;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathSegmentImplTest {

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new PathSegmentImpl(null));
        assertThrows(IllegalArgumentException.class, () -> new PathSegmentImpl(""));
        assertDoesNotThrow(() -> new PathSegmentImpl("abc;x=y;m=n"));
    }

    @Test
    void testBasic() {
        PathSegment segment = new PathSegmentImpl("abc;x=y;m=n;x= y1 ; m = n1");
        assertEquals("abc", segment.getPath());
        MultivaluedMap<String, String> matrixParams = segment.getMatrixParameters();
        assertEquals(2, matrixParams.keySet().size());
        assertEquals(2, matrixParams.get("x").size());
        assertEquals("y", matrixParams.get("x").get(0));
        assertEquals("y1", matrixParams.get("x").get(1));

        assertEquals(2, matrixParams.get("m").size());
        assertEquals("n", matrixParams.get("m").get(0));
        assertEquals("n1", matrixParams.get("m").get(1));
    }

}

