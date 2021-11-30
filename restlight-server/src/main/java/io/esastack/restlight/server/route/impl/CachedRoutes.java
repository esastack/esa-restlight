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
package io.esastack.restlight.server.route.impl;

import java.util.Arrays;
import java.util.Comparator;

interface CachedRoutes extends Routes<CountedRoute> {

    /**
     * Reverse order
     */
    Comparator<CountedRoute> COMPARATOR
            = (x, y) -> Long.compare(y.snapshot, x.snapshot);

    /**
     * Resets the values of {@link #lookup()} if necessary.
     */
    void hit(CountedRoute r);

    /**
     * Adds the given {@code r} so that it can be found from {@link #lookup()}.
     *
     * @param r r
     */
    void add(CountedRoute r);

    /**
     * Removes the given {@code r} so that it's can't be found from {@link #lookup()}.
     *
     * @param r r
     */
    void remove(CountedRoute r);

    static CountedRoute[] compute(CountedRoute[] current) {
        CountedRoute[] tmp = Arrays.copyOf(current, current.length);
        for (CountedRoute hmm : tmp) {
            hmm.markAndReset();
        }
        Arrays.sort(tmp, COMPARATOR);
        return tmp;
    }
}
