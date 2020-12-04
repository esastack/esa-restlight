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
package esa.restlight.server.route.impl;

import esa.commons.concurrent.UnsafeUtils;
import io.netty.util.internal.InternalThreadLocalMap;
import sun.misc.Unsafe;

class UnsafeCachedRoutes extends RhsPadding implements CachedRoutes {

    private final int rate;

    private static final Unsafe UNSAFE = UnsafeUtils.getUnsafe();
    private static final long CACHE_OFFSET
            = UnsafeUtils.objectFieldOffset(CacheValue.class, "cache");
    private static final long CTL_OFFSET
            = UnsafeUtils.objectFieldOffset(CtlValue.class, "ctl");

    UnsafeCachedRoutes(CountedRoute[] routes, int computeRate) {
        if (computeRate < 0 || computeRate > 1000) {
            throw new IllegalArgumentException("Compute rate must between 0 and 1000");
        }
        this.rate = computeRate;
        this.cache = routes;
    }

    @Override
    public void hit(CountedRoute r) {
        r.hits.increment();
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
    public CountedRoute[] lookup() {
        return cache;
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
