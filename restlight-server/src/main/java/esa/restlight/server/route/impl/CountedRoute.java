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

import esa.restlight.server.route.Route;

import java.util.concurrent.atomic.LongAdder;

class CountedRoute extends RouteWrap {

    static final long MIN_HITS = 4;
    LongAdder hits = new LongAdder();
    long snapshot;

    CountedRoute(Route route) {
        super(route);
    }

    void markAndReset() {
        long s = hits.sum();
        if (s > MIN_HITS) {
            // Reduces every hits by half of its original value.
            hits.add(-(s >> 1));
        } else {
            hits.reset();
        }
        snapshot = s;
    }
}
