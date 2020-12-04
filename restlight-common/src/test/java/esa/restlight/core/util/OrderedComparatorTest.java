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
package esa.restlight.core.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderedComparatorTest {

    @Test
    void testOrdered() {
        final Ordered foo = new Ordered() {
            @Override
            public int getOrder() {
                return 1;
            }
        };

        final Ordered bar = new Ordered() {
            @Override
            public int getOrder() {
                return 0;
            }
        };

        final List<Ordered> forSort = new ArrayList<>();
        forSort.add(foo);
        forSort.add(bar);
        OrderedComparator.sort(forSort);
        assertEquals(2, forSort.size());
        assertEquals(bar, forSort.get(0));
        assertEquals(foo, forSort.get(1));
    }


    @Test
    void testGetOrder() {
        final Object noneOrdered = new Object();
        final Ordered foo = new Ordered() {
            @Override
            public int getOrder() {
                return 1;
            }
        };

        assertEquals(Ordered.LOWEST_PRECEDENCE, OrderedComparator.getOrder(noneOrdered));
        assertEquals(foo.getOrder(), OrderedComparator.getOrder(foo));
    }

}
