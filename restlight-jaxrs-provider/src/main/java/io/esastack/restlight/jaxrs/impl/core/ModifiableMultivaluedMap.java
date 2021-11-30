
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

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpHeaders;
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModifiableMultivaluedMap implements MultivaluedMap<String, String> {

    private final HttpHeaders underlying;

    public ModifiableMultivaluedMap(HttpHeaders underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public void putSingle(String key, String value) {
        underlying.set(key, value);
    }

    @Override
    public void add(String key, String value) {
        underlying.add(key, value);
    }

    @Override
    public String getFirst(String key) {
        return underlying.get(key);
    }

    @Override
    public void addAll(String key, String... newValues) {
        Checks.checkNotNull(newValues, "newValues");
        if (newValues.length == 0) {
            return;
        }
        underlying.add(key, newValues);
    }

    @Override
    public void addAll(String key, List<String> valueList) {
        Checks.checkNotNull(valueList, "valueList");
        if (valueList.size() == 0) {
            return;
        }
        underlying.add(key, valueList);
    }

    @Override
    public void addFirst(String key, String value) {
        List<String> previous = underlying.getAll(key);
        if (previous == null || previous.isEmpty()) {
            previous = new ArrayList<>();
        }
        previous.add(0, value);
        underlying.set(key, previous);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
        return RuntimeDelegateUtils.equalsIgnoreValueOrder(this, otherMap);
    }

    @Override
    public int size() {
        return underlying.size();
    }

    @Override
    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        Checks.checkNotNull(key, "key");
        for (String name : underlying.names()) {
            if (name.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Checks.checkNotNull(value, "value");
        for (String name : underlying.names()) {
            if (value.equals(underlying.getAll(name))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> get(Object key) {
        Checks.checkNotNull(key, "key");
        return underlying.getAll(key.toString());
    }

    @Override
    public List<String> put(String key, List<String> value) {
        List<String> previous = underlying.getAll(key);
        underlying.set(key, value);
        return previous;
    }

    @Override
    public List<String> remove(Object key) {
        Checks.checkNotNull(key, "key");
        List<String> previous = underlying.getAll(key.toString());
        underlying.remove(key.toString());
        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        clear();
        for (Map.Entry<? extends String, ? extends List<String>> entry : m.entrySet()) {
            underlying.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        underlying.clear();
    }

    @Override
    public Set<String> keySet() {
        return underlying.names();
    }

    @Override
    public Collection<List<String>> values() {
        List<List<String>> values = new LinkedList<>();
        for (Entry<String, List<String>> entry : entrySet()) {
            values.add(entry.getValue());
        }
        return values;
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        Set<Entry<String, List<String>>> entries = new LinkedHashSet<>();
        for (String name : underlying.names()) {
            entries.add(new Entry<String, List<String>>() {
                @Override
                public String getKey() {
                    return name;
                }

                @Override
                public List<String> getValue() {
                    return underlying.getAll(name);
                }

                @Override
                public List<String> setValue(List<String> value) {
                    List<String> previous = getValue();
                    underlying.set(name, value);
                    return previous;
                }
            });
        }
        return entries;
    }
}

