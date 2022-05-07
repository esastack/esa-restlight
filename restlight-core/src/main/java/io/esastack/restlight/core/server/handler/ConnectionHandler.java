/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.server.handler;

import io.esastack.restlight.core.server.Connection;
import io.esastack.restlight.core.util.Ordered;

@FunctionalInterface
public interface ConnectionHandler extends Ordered {

    /**
     * This callback notification is invoked in case a new {@link Connection} connected successfully.
     *
     * @param connection connection
     */
    void onConnect(Connection connection);

}

