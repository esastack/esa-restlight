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
package esa.restlight.core.interceptor;

import esa.commons.spi.SPI;

/**
 * We will determine that whether a {@link esa.httpserver.core.AsyncRequest} should be matched to this interceptor by
 * the {@link InterceptorPredicate#test(Object)} when a {@link esa.httpserver.core.AsyncRequest} is coming.
 * @see InternalInterceptor
 * @see InterceptorPredicate
 * @see InterceptorFactory#of(MappingInterceptor)
 */
@SPI
public interface MappingInterceptor extends InternalInterceptor, InterceptorPredicate {
}
