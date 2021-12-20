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
package io.esastack.restlight.server.core.impl;

import esa.commons.Checks;
import esa.commons.collection.Attribute;
import esa.commons.collection.AttributeKey;
import esa.commons.collection.Attributes;

import java.util.function.BiConsumer;

public class AttributesProxy implements Attributes {

    private final Attributes underlying;

    public AttributesProxy(Attributes underlying) {
        Checks.checkNotNull(underlying, "underlying");
        this.underlying = underlying;
    }

    @Override
    public <V> Attribute<V> attr(AttributeKey<V> key) {
        return underlying.attr(key);
    }

    @Override
    public boolean hasAttr(AttributeKey<?> key) {
        return underlying.hasAttr(key);
    }

    @Override
    public void forEach(BiConsumer<? super AttributeKey<?>, ? super Attribute<?>> consumer) {
        underlying.forEach(consumer);
    }

    @Override
    public int size() {
        return underlying.size();
    }

    @Override
    public boolean isEmpty() {
        return underlying.isEmpty();
    }

}

