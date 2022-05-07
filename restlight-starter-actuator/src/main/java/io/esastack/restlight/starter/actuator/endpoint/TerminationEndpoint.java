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
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.server.NettyRestlightServer;
import io.esastack.restlight.core.server.RestlightServer;
import io.esastack.restlight.core.server.processor.FilteredHandler;
import io.esastack.restlight.core.server.processor.RestlightHandler;
import io.esastack.restlight.core.server.processor.schedule.ScheduledRestlightHandler;
import io.esastack.restlight.starter.ServerStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import static io.esastack.restlight.starter.actuator.endpoint.Utils.findField;
import static io.esastack.restlight.starter.actuator.endpoint.Utils.findServer;

@Endpoint(id = "terminationtimeout")
public class TerminationEndpoint {

    private static final Logger logger =
            LoggerFactory.getLogger(TerminationEndpoint.class);

    @Autowired(required = false)
    @Qualifier(Constants.SERVER)
    private ServerStarter server;

    @WriteOperation
    public String setTerminationTimeout(long timeout) {
        if (timeout < 0) {
            return "Failed: timeout seconds must over than zero!";
        }
        if (server != null) {
            RestlightServer theServer = findServer(server);
            if (theServer instanceof NettyRestlightServer) {
                RestlightHandler handler =
                        findField(theServer, "handler", RestlightHandler.class)
                                .orElse(null);
                if (handler instanceof ScheduledRestlightHandler) {
                    ScheduledRestlightHandler dispatcherHandler =
                            (ScheduledRestlightHandler) handler;
                    dispatcherHandler.setTerminationTimeoutSeconds(timeout);
                    logger.info("Change termination timeout of dispatcher handler to {}s", timeout);
                    return "Success";
                } else if (handler instanceof FilteredHandler) {
                    handler = findField(handler, "delegate", RestlightHandler.class).orElse(null);
                    if (handler instanceof ScheduledRestlightHandler) {
                        ScheduledRestlightHandler dispatcherHandler =
                                (ScheduledRestlightHandler) handler;
                        dispatcherHandler.setTerminationTimeoutSeconds(timeout);
                        logger.info("Change termination timeout of dispatcher handler to {}s", timeout);
                        return "Success";
                    }
                }
            }
        }
        return "Failed: unsupported operation!";
    }

}
