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
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UnmodifiableMultivaluedMap<K, V> implements MultivaluedMap<K, V> {

    private static final UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException();

    private final MultivaluedMap<K, V> underlying;

    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, List<V>>> entrySet;
    private transient Collection<List<V>> values;

    public UnmodifiableMultivaluedMap(MultivaluedMap<K, V> underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public void putSingle(K key, V value) {
        throw UNSUPPORTED;
    }

    @Override
    public void add(K key, V value) {
        throw UNSUPPORTED;
    }

    @Override
    public V getFirst(K key) {
        return underlying.getFirst(key);
    }

    @Override
    public void addAll(K key, V... newValues) {
        throw UNSUPPORTED;
    }

    @Override
    public void addAll(K key, List<V> valueList) {
        throw UNSUPPORTED;
    }

    @Override
    public void addFirst(K key, V value) {
        throw UNSUPPORTED;
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> otherMap) {
        return underlying.equalsIgnoreValueOrder(otherMap);
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
    public List<V> get(Object key) {
        return underlying.get(key);
    }

    @Override
    public List<V> put(K key, List<V> value) {
        throw UNSUPPORTED;
    }

    @Override
    public List<V> remove(Object key) {
        throw UNSUPPORTED;
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        throw UNSUPPORTED;
    }

    @Override
    public void clear() {
        throw UNSUPPORTED;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = Collections.unmodifiableSet(underlying.keySet());
        }
        return keySet;
    }

    @Override
    public Collection<List<V>> values() {
        if (values == null) {
            values = Collections.unmodifiableCollection(underlying.values());
        }
        return values;
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        if (entrySet == null) {
            entrySet = Collections.unmodifiableSet(underlying.entrySet());
        }
        return entrySet;
    }
}

