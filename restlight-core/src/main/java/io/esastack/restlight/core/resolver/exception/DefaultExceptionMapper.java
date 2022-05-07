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
package io.esastack.restlight.core.resolver.exception;

import esa.commons.Checks;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This implementation of {@link ExceptionMapper} holds given {@link #mappings} and maps these {@link #mappings} by
 * exception type and {@link DepthComparator} if more than one {@link ExceptionResolver} matched.
 */
public class DefaultExceptionMapper implements ExceptionMapper {

    /**
     * Exception type -> RouteHandlerImpl
     */
    private final Map<Class<? extends Throwable>, ? extends ExceptionResolver<Throwable>> mappings;

    public DefaultExceptionMapper(Map<Class<? extends Throwable>, ? extends ExceptionResolver<Throwable>> mappings) {
        Checks.checkNotEmptyArg(mappings);
        this.mappings = mappings;
    }

    @Override
    public ExceptionResolver<Throwable> mapTo(Class<? extends Throwable> type) {
        if (type == null) {
            return null;
        }
        ExceptionResolver<Throwable> ha;
        if ((ha = this.mappings.get(type)) != null) {
            return ha;
        }

        List<Class<? extends Throwable>> matches =
                InternalThreadLocalMap.get().arrayList(mappings.size());
        for (Map.Entry<Class<? extends Throwable>, ? extends ExceptionResolver<Throwable>> entry :
                this.mappings.entrySet()) {
            Class<? extends Throwable> mappedType = entry.getKey();
            if (mappedType.isAssignableFrom(type)) {
                matches.add(mappedType);
            }
        }

        if (!matches.isEmpty()) {
            matches.sort(new DepthComparator(type));
            return this.mappings.get(matches.get(0));
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private static class DepthComparator
            implements Comparator<Class<? extends Throwable>> {

        final Class<? extends Throwable> targetException;

        DepthComparator(Class<? extends Throwable> exception) {
            targetException = exception;
        }

        @Override
        public int compare(Class<? extends Throwable> o1, Class<? extends Throwable> o2) {
            return (getDepthRecursively(o1, this.targetException, 0)
                    - getDepthRecursively(o2, this.targetException, 0));
        }

        private static int getDepthRecursively(Class<?> ex, Class<?> target, int depth) {
            if (target.equals(ex)) {
                return depth;
            }
            if (target == Throwable.class) {
                return Integer.MAX_VALUE;
            }
            return getDepthRecursively(ex, target.getSuperclass(), depth + 1);
        }
    }
}
