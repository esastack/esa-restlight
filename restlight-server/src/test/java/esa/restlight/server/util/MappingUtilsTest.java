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

import esa.restlight.core.method.HttpMethod;
import esa.restlight.server.route.Mapping;
import esa.restlight.server.route.predicate.ConsumesPredicate;
import esa.restlight.server.route.predicate.ProducesPredicate;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MappingUtilsTest {

    @Test
    void testCombine() {
        final Mapping parent = Mapping.mapping()
                .name("parent")
                .path("/parent")
                .method("get")
                .params("p=1")
                .headers("p=1")
                .consumes("text/plain")
                .produces("text/plain");

        final Mapping child = Mapping.mapping()
                .name("child")
                .path("/child")
                .method("post")
                .params("q=1")
                .headers("q=1")
                .consumes("text/html")
                .produces("text/html");

        final Mapping combined = MappingUtils.combine(parent, child);
        assertEquals("child", combined.name());
        assertNotNull(combined.path());
        assertEquals(1, combined.path().length);
        assertEquals("/parent/child", combined.path()[0]);
        assertNotNull(combined.method());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET, HttpMethod.POST}, combined.method());
        assertNotNull(combined.params());
        assertArrayEquals(new String[]{"p=1", "q=1"}, combined.params());
        assertNotNull(combined.headers());
        assertArrayEquals(new String[]{"p=1", "q=1"}, combined.headers());
        assertNotNull(combined.consumes());
        assertEquals("text/html", combined.consumes()[0]);
        assertNotNull(combined.produces());
        assertEquals("text/html", combined.produces()[0]);
    }

    @Test
    void testCombineMethod() {
        final Mapping parent = Mapping.mapping()
                .method(HttpMethod.GET, HttpMethod.POST);

        final Mapping child = Mapping.mapping()
                .method(HttpMethod.POST, HttpMethod.DELETE);

        final Mapping combined = MappingUtils.combine(parent, child);

        assertNotNull(combined.method());
        assertArrayEquals(new HttpMethod[]{HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE}, combined.method());
        assertNotNull(combined.params());
    }

    @Test
    void testCombineParams() {
        final Mapping parent = Mapping.mapping()
                .params("a=1", "b=2");

        final Mapping child = Mapping.mapping()
                .params("b=2", "c=3");

        final Mapping combined = MappingUtils.combine(parent, child);

        assertNotNull(combined.params());
        assertArrayEquals(new String[]{"a=1", "b=2", "c=3"}, combined.params());
    }

    @Test
    void testCombineHeaders() {
        final Mapping parent = Mapping.mapping()
                .headers("a=1", "b=2");

        final Mapping child = Mapping.mapping()
                .headers("b=2", "c=3");

        final Mapping combined = MappingUtils.combine(parent, child);

        assertNotNull(combined.headers());
        assertArrayEquals(new String[]{"a=1", "b=2", "c=3"}, combined.headers());
    }

    @Test
    void testCombineConsumes() {
        final Mapping parent = Mapping.mapping()
                .consumes("a/1", "b/2");

        final Mapping child = Mapping.mapping()
                .consumes("b/2", "c/3");

        final Mapping combined = MappingUtils.combine(parent, child);

        assertNotNull(combined.consumes());
        assertArrayEquals(new String[]{"b/2", "c/3"}, combined.consumes());
    }

    @Test
    void testCombineProduces() {
        final Mapping parent = Mapping.mapping()
                .produces("a/1", "b/2");

        final Mapping child = Mapping.mapping()
                .produces("b/2", "c/3");

        final Mapping combined = MappingUtils.combine(parent, child);

        assertNotNull(combined.produces());
        assertArrayEquals(new String[]{"b/2", "c/3"}, combined.produces());
    }

    @Test
    void testCombineConsumesAndHeaders() {
        final Mapping parent = Mapping.mapping()
                .headers("content-type=a/1")
                .consumes("a/2", "a/3");
        final Mapping child = Mapping.mapping()
                .headers("content-type=b/1")
                .consumes("b/2", "b/3");
        final Mapping combined = MappingUtils.combine(parent, child);
        assertNotNull(combined.headers());
        assertNotNull(combined.consumes());
        assertArrayEquals(new String[]{"content-type=b/1"}, combined.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined.consumes());

        final Mapping parent1 = Mapping.mapping()
                .headers("content-type=a/1", "a/0")
                .consumes("a/2", "a/3");
        final Mapping child1 = Mapping.mapping()
                .headers("content-type=b/1")
                .consumes("b/2", "b/3");
        final Mapping combined1 = MappingUtils.combine(parent1, child1);
        assertNotNull(combined1.headers());
        assertNotNull(combined1.consumes());
        assertArrayEquals(new String[]{"a/0", "content-type=b/1"}, combined1.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined1.consumes());

        final Mapping parent2 = Mapping.mapping()
                .headers("content-type=a/1", "a/0")
                .consumes("a/2", "a/3");
        final Mapping child2 = Mapping.mapping()
                .consumes("b/2", "b/3");
        final Mapping combined2 = MappingUtils.combine(parent2, child2);
        assertNotNull(combined2.headers());
        assertNotNull(combined2.consumes());
        assertArrayEquals(new String[]{"a/0"}, combined2.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined2.consumes());

        final Mapping parent3 = Mapping.mapping()
                .headers("content-type=a/1", "a/0")
                .consumes("a/2", "a/3");
        final Mapping child3 = Mapping.mapping();
        final Mapping combined3 = MappingUtils.combine(parent3, child3);
        assertNotNull(combined3.headers());
        assertNotNull(combined3.consumes());
        assertArrayEquals(new String[]{"content-type=a/1", "a/0"}, combined3.headers());
        assertArrayEquals(new String[]{"a/2", "a/3"}, combined3.consumes());

        final Mapping parent4 = Mapping.mapping()
                .headers("content-type=a/1", "a/0")
                .consumes("a/2", "a/3");
        final Mapping child4 = Mapping.mapping()
                .headers("content-type=b/1");
        final Mapping combined4 = MappingUtils.combine(parent4, child4);
        assertNotNull(combined4.headers());
        assertNotNull(combined4.consumes());
        assertArrayEquals(new String[]{"a/0", "content-type=b/1"}, combined4.headers());
        assertArrayEquals(new String[]{}, combined4.consumes());
    }

    @Test
    void testCombineProducesAndHeaders() {
        final Mapping parent = Mapping.mapping()
                .headers("accept=a/1")
                .produces("a/2", "a/3");
        final Mapping child = Mapping.mapping()
                .headers("accept=b/1")
                .produces("b/2", "b/3");
        final Mapping combined = MappingUtils.combine(parent, child);
        assertNotNull(combined.headers());
        assertNotNull(combined.produces());
        assertArrayEquals(new String[]{"accept=b/1"}, combined.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined.produces());

        final Mapping parent1 = Mapping.mapping()
                .headers("accept=a/1", "a/0")
                .produces("a/2", "a/3");
        final Mapping child1 = Mapping.mapping()
                .headers("accept=b/1")
                .produces("b/2", "b/3");
        final Mapping combined1 = MappingUtils.combine(parent1, child1);
        assertNotNull(combined1.headers());
        assertNotNull(combined1.produces());
        assertArrayEquals(new String[]{"a/0", "accept=b/1"}, combined1.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined1.produces());

        final Mapping parent2 = Mapping.mapping()
                .headers("accept=a/1", "a/0")
                .produces("a/2", "a/3");
        final Mapping child2 = Mapping.mapping()
                .produces("b/2", "b/3");
        final Mapping combined2 = MappingUtils.combine(parent2, child2);
        assertNotNull(combined2.headers());
        assertNotNull(combined2.produces());
        assertArrayEquals(new String[]{"a/0"}, combined2.headers());
        assertArrayEquals(new String[]{"b/2", "b/3"}, combined2.produces());

        final Mapping parent3 = Mapping.mapping()
                .headers("accept=a/1", "a/0")
                .produces("a/2", "a/3");
        final Mapping child3 = Mapping.mapping();
        final Mapping combined3 = MappingUtils.combine(parent3, child3);
        assertNotNull(combined3.headers());
        assertNotNull(combined3.produces());
        assertArrayEquals(new String[]{"accept=a/1", "a/0"}, combined3.headers());
        assertArrayEquals(new String[]{"a/2", "a/3"}, combined3.produces());

        final Mapping parent4 = Mapping.mapping()
                .headers("accept=a/1", "a/0")
                .produces("a/2", "a/3");
        final Mapping child4 = Mapping.mapping()
                .headers("accept=b/1");
        final Mapping combined4 = MappingUtils.combine(parent4, child4);
        assertNotNull(combined4.headers());
        assertNotNull(combined4.produces());
        assertArrayEquals(new String[]{"a/0", "accept=b/1"}, combined4.headers());
        assertArrayEquals(new String[]{}, combined4.produces());
    }

    @Test
    void testParseConsumeExpression() {
        final Set<ConsumesPredicate.Expression> expressions
                = MappingUtils.parseConsumeExpressions(new String[]{"a/1"}, new String[]{"b=1"});
        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertEquals("a/1", expressions.iterator().next().getMediaType().value());

        final Set<ConsumesPredicate.Expression> expressions1
                = MappingUtils.parseConsumeExpressions(new String[]{"a/1"}, new String[]{"b=1", "content-type=b/1"});
        assertNotNull(expressions1);
        assertEquals(2, expressions1.size());
        assertTrue(expressions1.contains(new ConsumesPredicate.Expression("a/1")));
        assertTrue(expressions1.contains(new ConsumesPredicate.Expression("b/1")));
    }

    @Test
    void testParseProduceExpression() {
        final Set<ProducesPredicate.Expression> expressions
                = MappingUtils.parseProduceExpressions(new String[]{"a/1"}, new String[]{"b=1"});
        assertNotNull(expressions);
        assertEquals(1, expressions.size());
        assertEquals("a/1", expressions.iterator().next().getMediaType().value());

        final Set<ProducesPredicate.Expression> expressions1
                = MappingUtils.parseProduceExpressions(new String[]{"a/1"}, new String[]{"b=1", "accept=b/1"});
        assertNotNull(expressions1);
        assertEquals(2, expressions1.size());
        assertTrue(expressions1.contains(new ProducesPredicate.Expression("a/1")));
        assertTrue(expressions1.contains(new ProducesPredicate.Expression("b/1")));
    }

    @Test
    void testIsIntersect() {
        assertFalse(MappingUtils.isIntersect(null, null));
        final String[] empty1 = {};
        final String[] empty2 = {};
        assertFalse(MappingUtils.isIntersect(empty1, empty2));
        assertFalse(MappingUtils.isIntersect(empty1, null));
        assertFalse(MappingUtils.isIntersect(null, empty2));

        final String[] a = {"a"};
        final String[] b = {"b"};
        assertFalse(MappingUtils.isIntersect(a, b));
        assertFalse(MappingUtils.isIntersect(a, null));
        assertFalse(MappingUtils.isIntersect(null, b));
        assertFalse(MappingUtils.isIntersect(empty1, b));
        assertFalse(MappingUtils.isIntersect(a, empty2));

        final String[] c = {"c"};
        final String[] d = {"c"};
        assertTrue(MappingUtils.isIntersect(c, d));

        final String[] e = {"e", "b"};
        final String[] f = {"e", "f"};
        assertTrue(MappingUtils.isIntersect(e, f));
    }

}
