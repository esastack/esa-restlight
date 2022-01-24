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
package io.esastack.restlight.server.schedule;

import esa.commons.annotation.Beta;
import esa.commons.spi.SPI;
import io.esastack.restlight.core.util.Ordered;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.handler.Filter;

/**
 * Hook of every {@link RequestTask} which will be created when a request is coming after the execution of {@link
 * Filter} and about to be submitted to Biz-{@link Scheduler}.
 * <p>
 * Note: please do not use this interface unless you know what you are doing.
 */
@SPI
@Beta
public interface RequestTaskHook extends Ordered {

    /**
     * Returns the argument that was passed or a modified(possibly new) instance of {@link RequestTask} which will be
     * submitted to Biz-{@link Scheduler}.
     * <p>
     * Note: ensure the completeness of the {@link RequestTask} you returned, otherwise something wrong would happen in
     * next execution phase. Restlight will not check the completeness of the {@link RequestTask} you returned unless a
     * {@code null} value is returned which means {@link RequestTask} has been rejected and Restlight will try to
     * complete the {@link RequestTask} that was passed as much as possible.
     * <p>
     * bad case: return a instance of {@link RequestTask} which returns a {@code null} value by {@link
     * RequestContext#request()} and {@link RequestContext#response()} or {@link RequestTask#promise()}
     *
     * @param task request task
     * @return argument that was passed or a modified(possibly new) instance. {@link RequestTask} will not be submitted
     * to Biz-{@link Scheduler} if {@code null} is returned which means {@link RequestTask} has been rejected.
     */
    RequestTask onRequest(RequestTask task);

}
