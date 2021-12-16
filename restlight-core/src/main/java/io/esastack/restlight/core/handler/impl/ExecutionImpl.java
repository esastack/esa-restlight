/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restlight.core.handler.impl;

import esa.commons.Checks;
import io.esastack.restlight.server.route.CompletionHandler;
import io.esastack.restlight.server.route.Execution;
import io.esastack.restlight.server.route.ExecutionHandler;

public class ExecutionImpl implements Execution {

    private final ExecutionHandler executionHandler;
    private final CompletionHandler completionHandler;

    public ExecutionImpl(ExecutionHandler executionHandler) {
        this(executionHandler, null);
    }

    public ExecutionImpl(ExecutionHandler executionHandler,
                         CompletionHandler completionHandler) {
        Checks.checkNotNull(executionHandler, "executionHandler");
        this.executionHandler = executionHandler;
        this.completionHandler = completionHandler;
    }

    @Override
    public ExecutionHandler executionHandler() {
        return executionHandler;
    }

    @Override
    public CompletionHandler completionHandler() {
        return completionHandler;
    }
}

