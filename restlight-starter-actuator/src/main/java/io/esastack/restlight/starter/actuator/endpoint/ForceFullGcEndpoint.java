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
package io.esastack.restlight.starter.actuator.endpoint;

import esa.commons.logging.Logger;
import esa.commons.logging.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

@Endpoint(id = "forcefgc")
public class ForceFullGcEndpoint {

    private static final Logger logger =
            LoggerFactory.getLogger(ForceFullGcEndpoint.class);

    @WriteOperation
    public void forceFullGc() {
        logger.info("Force to trigger full gc by System.gc()");
        System.gc();
    }

}
