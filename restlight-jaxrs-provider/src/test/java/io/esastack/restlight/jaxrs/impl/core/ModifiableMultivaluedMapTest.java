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

import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.commons.net.netty.http.Http1HeadersImpl;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifiableMultivaluedMapTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new ModifiableMultivaluedMap(null));
        assertDoesNotThrow(() -> new ModifiableMultivaluedMap(new Http1HeadersImpl()));
    }

    @Test
    void testBasic() {
        final HttpHeaders headers = new Http1HeadersImpl();
        final ModifiableMultivaluedMap delegate = new ModifiableMultivaluedMap(headers);
        assertEquals(headers.size(), delegate.size());
        assertEquals(headers.isEmpty(), delegate.isEmpty());

        delegate.putSingle("name", "value1");
        assertEquals(1, headers.size());
        assertEquals("value1", headers.get("name"));

        delegate.add("name", "value2");
        assertEquals(headers.size(), delegate.size());
        List<String> values1 = headers.getAll("name");
        assertEquals(2, values1.size());
        assertEquals("value1", values1.get(0));
        assertEquals("value2", values1.get(1));
        assertEquals("value1", delegate.getFirst("name"));

        assertThrows(NullPointerException.class, () -> delegate.addAll("name", (String[]) null));
        delegate.addAll("name", "value3", "value4");
        assertEquals(4, headers.getAll("name").size());
        assertTrue(headers.getAll("name").contains("value3"));
        assertTrue(headers.getAll("name").contains("value4"));

        assertThrows(NullPointerException.class, () -> delegate.addAll("name", (List<String>) null));
        delegate.addAll("name", "value5");
        delegate.addAll("name", "value6");
        assertEquals(6, headers.getAll("name").size());
        assertTrue(headers.getAll("name").contains("value5"));
        assertTrue(headers.getAll("name").contains("value6"));

        delegate.addFirst("name", "value7");
        assertEquals(7, headers.getAll("name").size());
        assertEquals("value7", headers.get("name"));
        delegate.addFirst("name1", "value1");
        assertEquals(1, headers.getAll("name1").size());
        assertEquals("value1", headers.get("name1"));

        assertFalse(delegate.equalsIgnoreValueOrder(null));
        assertFalse(delegate.equalsIgnoreValueOrder(new MultivaluedHashMap<>()));
        MultivaluedMap<String, String> otherMap = new MultivaluedHashMap<>();
        otherMap.addAll("name", "value7", "value1", "value2", "value3", "value4", "value5", "value6");
        assertFalse(delegate.equalsIgnoreValueOrder(otherMap));
        otherMap.add("name1", "value1");
        assertTrue(delegate.equalsIgnoreValueOrder(otherMap));
        otherMap.add("name1", "value2");
        assertFalse(delegate.equalsIgnoreValueOrder(otherMap));

        assertEquals(headers.size(), delegate.size());
        assertEquals(headers.isEmpty(), delegate.isEmpty());

        assertTrue(delegate.containsKey("name"));
        assertTrue(delegate.containsKey("name1"));
        assertFalse(delegate.containsKey("name2"));

        assertTrue(delegate.containsValue("value1"));
        assertTrue(delegate.containsValue("value2"));
        assertFalse(delegate.containsValue("value8"));

        List<String> values2 = delegate.get("name");
        assertEquals(7, values2.size());
        List<String> values3 = delegate.get("name1");
        assertEquals(1, values3.size());
        assertTrue(delegate.get("name2").isEmpty());

        List<String> values4 = delegate.put("name", Collections.singletonList("value8"));
        assertNotNull(values4);
        assertEquals(7, values4.size());
        assertFalse(values4.contains("value8"));
        assertEquals("value8", delegate.getFirst("name"));

        List<String> values5 = delegate.remove("name");
        assertEquals(1, values5.size());
        assertTrue(values5.contains("value8"));
        assertFalse(delegate.containsKey("name"));

        final Map<String, List<String>> toAdd = new HashMap<>();
        toAdd.put("name", Collections.singletonList("value"));
        toAdd.put("name1", Collections.singletonList("value9"));
        toAdd.put("name2", null);
        delegate.putAll(toAdd);
        assertEquals(1, delegate.get("name").size());
        assertEquals("value", delegate.getFirst("name"));
        assertEquals(2, delegate.get("name1").size());
        assertEquals("value9", delegate.get("name1").get(0));
        assertEquals("value1", delegate.get("name1").get(1));

        assertEquals(2, delegate.keySet().size());
        assertTrue(delegate.keySet().contains("name"));
        assertTrue(delegate.keySet().contains("name1"));

        List<List<String>> values6 = new ArrayList<>(delegate.values());
        assertEquals(2, values6.size());
        assertEquals(1, values6.get(0).size());
        assertEquals("value", values6.get(0).get(0));
        assertEquals(2, values6.get(1).size());
        assertEquals("value9", values6.get(1).get(0));
        assertEquals("value1", values6.get(1).get(1));

        Set<Map.Entry<String, List<String>>> entries = delegate.entrySet();
        assertEquals(2, entries.size());
        List<Map.Entry<String, List<String>>> entryList = new ArrayList<>(entries);
        assertEquals("name", entryList.get(0).getKey());
        assertEquals(1, entryList.get(0).getValue().size());
        assertEquals("value", entryList.get(0).getValue().get(0));
        assertEquals(2, entryList.get(1).getValue().size());
        assertEquals("value9", entryList.get(1).getValue().get(0));
        assertEquals("value1", entryList.get(1).getValue().get(1));

        delegate.clear();
        assertEquals(0, delegate.keySet().size());
        assertEquals(0, delegate.values().size());
    }

}

