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
package io.esastack.restlight.core.route.impl;

import esa.commons.concurrent.UnsafeUtils;
import io.netty.util.internal.InternalThreadLocalMap;
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class UnsafeCachedRoutes extends RhsPadding implements CachedRoutes {

    private final int rate;

    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();
    private static final long CACHE_OFFSET
            = UnsafeUtils.objectFieldOffset(CacheValue.class, "cache");
    private static final long CTL_OFFSET
            = UnsafeUtils.objectFieldOffset(CtlValue.class, "ctl");

    UnsafeCachedRoutes(int computeRate) {
        if (computeRate < 0 || computeRate > 1000) {
            throw new IllegalArgumentException("Compute rate must between 0 and 1000");
        }
        this.rate = computeRate;
        this.cache = new CountedRoute[0];
    }

    @Override
    public void hit(CountedRoute rs) {
        rs.hits.increment();
        if (InternalThreadLocalMap.get().random().nextInt(1000) < rate) {
            if (UNSAFE.compareAndSwapInt(this, CTL_OFFSET, 0, 1)) {
                // StoreStore
                UNSAFE.putOrderedObject(this, CACHE_OFFSET, CachedRoutes.compute(cache));
                // StoreStore
                UNSAFE.putIntVolatile(this, CTL_OFFSET, 0);
                // StoreLoad
            }
        }
    }

    @Override
    public void add(CountedRoute r) {
        CountedRoute[] rs = (CountedRoute[]) UNSAFE.getObjectVolatile(this, CACHE_OFFSET);
        CountedRoute[] updated = Arrays.copyOf(rs, rs.length + 1);
        updated[updated.length - 1] = r;
        UNSAFE.putOrderedObject(this, CACHE_OFFSET, updated);
    }

    @Override
    public void remove(CountedRoute r) {
        CountedRoute[] rs = (CountedRoute[]) UNSAFE.getObjectVolatile(this, CACHE_OFFSET);
        List<CountedRoute> updated = new LinkedList<>();
        for (CountedRoute item : rs) {
            if (!AbstractRouteRegistry.isEquals(r, item)) {
                updated.add(item);
            }
        }
        UNSAFE.putOrderedObject(this, CACHE_OFFSET, updated.toArray(new CountedRoute[0]));
    }

    @Override
    public CountedRoute[] lookup() {
        return (CountedRoute[]) UNSAFE.getObjectVolatile(this, CACHE_OFFSET);
    }
}

abstract class LhsPadding {

    /**
     * should be public
     */
    public long p01, p02, p03, p04, p05, p06, p07;
}

/**
 * Attempts to be more efficient with regards to false sharing by adding padding around the volatile field: {@link
 * #cache}.
 */
abstract class CacheValue extends LhsPadding {
    volatile CountedRoute[] cache;
}

abstract class RhsCachedPadding extends CacheValue {
    /**
     * should be public
     */
    public long p01, p02, p03, p04, p05, p06, p07;
}

/**
 * Attempts to be more efficient with regards to false sharing by adding padding around the volatile field: {@link
 * #ctl}.
 */
abstract class CtlValue extends RhsCachedPadding {
    volatile int ctl;
}

abstract class RhsPadding extends CtlValue {
    /**
     * should be public
     */
    public long p01, p02, p03, p04, p05, p06, p07;
}
