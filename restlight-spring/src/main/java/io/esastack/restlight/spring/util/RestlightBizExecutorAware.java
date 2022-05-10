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
package io.esastack.restlight.spring.util;

import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.spring.Restlight4Spring;
import org.springframework.beans.factory.Aware;

import java.util.concurrent.Executor;

/**
 * A marker interface indicating that a bean is eligible to be notified by the {@link
 * Restlight4Spring} after building and starting a {@link RestlightServer}.
 */
public interface RestlightBizExecutorAware extends Aware {

    /**
     * Sets {@link Executor} that is used as Biz executor in {@link Restlight4Spring}.
     * <p>
     * This method will use the return value of {@link Restlight4Spring#bizExecutor()} as the parameter.
     * <p>
     * Note: If there's no executor in  {@link Restlight4Spring}, this method will use the {@link
     * esa.commons.concurrent.DirectExecutor#INSTANCE} as the parameter.
     *
     * @param bizExecutor executor
     * @see RestlightServer#bizExecutor()
     */
    void setRestlightBizExecutor(Executor bizExecutor);

}
