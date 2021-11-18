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
import io.esastack.restlight.jaxrs.util.RuntimeDelegateUtils;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DelegatingMultivaluedMap implements MultivaluedMap<String, String> {

    private final MultivaluedMap<String, Object> underlying;

    public DelegatingMultivaluedMap(MultivaluedMap<String, Object> underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public void putSingle(String key, String value) {
        underlying.putSingle(key, value);
    }

    @Override
    public void add(String key, String value) {
        underlying.add(key, value);
    }

    @Override
    public String getFirst(String key) {
        return RuntimeDelegateUtils.toString(underlying.getFirst(key));
    }

    @Override
    public void addAll(String key, String... newValues) {
        underlying.addAll(key, newValues);
    }

    @Override
    public void addAll(String key, List<String> valueList) {
        underlying.addAll(key, valueList);
    }

    @Override
    public void addFirst(String key, String value) {
        underlying.addFirst(key, value);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, String> otherMap) {
        return RuntimeDelegateUtils.equalsIgnoreValueOrder(underlying, otherMap);
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
        return underlying.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return underlying.containsValue(value);
    }

    @Override
    public List<String> get(Object key) {
        List<Object> previous = underlying.get(key);
        if (previous == null) {
            return Collections.emptyList();
        }
        return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> put(String key, List<String> value) {
        final List<Object> newValue;
        if (value == null) {
            newValue = null;
        } else {
            newValue = new ArrayList<>(value);
        }
        List<Object> previous = underlying.put(key, newValue);
        if (previous == null) {
            return Collections.emptyList();
        }
        return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> remove(Object key) {
        List<Object> previous = underlying.remove(key);
        if (previous == null) {
            return Collections.emptyList();
        }
        return previous.stream().map(RuntimeDelegateUtils::toString).collect(Collectors.toList());
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> m) {
        Map<String, List<Object>> entries = new LinkedHashMap<>();
        m.forEach((k, v) -> entries.put(k, v == null ? null : new ArrayList<>(v)));
        underlying.putAll(entries);
    }

    @Override
    public void clear() {
        underlying.clear();
    }

    @Override
    public Set<String> keySet() {
        return underlying.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        Collection<List<String>> values = new ArrayList<>();
        underlying.values().forEach(vs -> values.add(vs.stream().map(RuntimeDelegateUtils::toString)
                .collect(Collectors.toList())));
        return values;
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        Set<Entry<String, List<String>>> entries = new LinkedHashSet<>();
        underlying.forEach((key, value) -> entries.add(new Entry<String, List<String>>() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public List<String> getValue() {
                return value.stream().map(RuntimeDelegateUtils::toString)
                        .collect(Collectors.toList());
            }

            @Override
            public List<String> setValue(List<String> value) {
                List<String> previous = getValue();
                underlying.put(key, value.stream().map(RuntimeDelegateUtils::toString)
                        .collect(Collectors.toList()));
                return previous;
            }
        }));
        return entries;
    }
}

