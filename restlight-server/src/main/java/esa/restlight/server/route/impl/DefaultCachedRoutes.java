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

import io.netty.util.internal.InternalThreadLocalMap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


class DefaultCachedRoutes implements CachedRoutes {

    private final int rate;
    private final AtomicBoolean ctl = new AtomicBoolean(false);
    private final AtomicReference<CountedRoute[]> cache;

    DefaultCachedRoutes(CountedRoute[] routes, int computeRate) {
        if (computeRate < 0 || computeRate > 1000) {
            throw new IllegalArgumentException("Compute rate must between 0 and 1000");
        }
        this.rate = computeRate;
        this.cache = new AtomicReference<>(routes);
    }

    @Override
    public void hit(CountedRoute r) {
        r.hits.increment();
        if (InternalThreadLocalMap.get().random().nextInt(1000) < rate) {
            if (ctl.compareAndSet(false, true)) {
                cache.lazySet(CachedRoutes.compute(cache.get()));
                ctl.set(false);
            }
        }
    }

    @Override
    public CountedRoute[] lookup() {
        return cache.get();
    }
}
