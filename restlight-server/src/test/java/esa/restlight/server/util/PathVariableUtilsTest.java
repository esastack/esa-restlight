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
package esa.restlight.server.util;

import esa.commons.collection.MultiValueMap;
import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.route.predicate.PatternsPredicate;
import esa.restlight.test.mock.MockAsyncRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathVariableUtilsTest {

    @Test
    void testGetTemplateVariables() {
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .build();
        assertNull(PathVariableUtils.getPathVariables(request));
        final Map<String, String> variables = new HashMap<>();
        variables.put("foo", "a");
        variables.put("bar", "b");
        request.setAttribute(PatternsPredicate.TEMPLATE_VARIABLES, variables);
        assertEquals(variables, PathVariableUtils.getPathVariables(request));
        assertEquals("a", PathVariableUtils.getPathVariable(request, "foo"));
        assertEquals("b", PathVariableUtils.getPathVariable(request, "bar"));
        assertNull(PathVariableUtils.getPathVariable(request, "baz"));

        assertEquals(variables, PathVariableUtils.getPathVariables(request));
    }

    @Test
    void testGetMatrixVariables() {
        final AsyncRequest request = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .build();
        final Map<String, String> variables = new HashMap<>();
        variables.put("foo", "a=1,2");
        variables.put("bar", "21;b=22;c=23;d=");
        request.setAttribute(PatternsPredicate.TEMPLATE_VARIABLES, variables);
        final Map<String, MultiValueMap<String, String>> matrix = PathVariableUtils.getMatrixVariables(request);
        assertNotNull(matrix);
        assertEquals(2, matrix.size());
        assertEquals(2, matrix.get("foo").get("a").size());
        assertEquals("1", matrix.get("foo").get("a").get(0));
        assertEquals("2", matrix.get("foo").get("a").get(1));
        assertEquals(1, matrix.get("bar").get("b").size());
        assertEquals("22", matrix.get("bar").get("b").get(0));
        assertEquals(1, matrix.get("bar").get("c").size());
        assertEquals("23", matrix.get("bar").get("c").get(0));
        assertFalse(matrix.get("bar").containsKey("d"));

        assertEquals(matrix, PathVariableUtils.getMatrixVariables(request));

        final AsyncRequest request1 = MockAsyncRequest.aMockRequest()
                .withUri("/")
                .build();
        assertNotNull(PathVariableUtils.getMatrixVariables(request1));
        assertTrue(PathVariableUtils.getMatrixVariables(request1).isEmpty());
    }

}
