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
package esa.restlight.starter.actuator.condition;

import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

import static esa.restlight.core.util.Constants.MANAGEMENT_SERVER_PORT;
import static esa.restlight.core.util.Constants.SERVER_PORT;
import static org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType.DIFFERENT;
import static org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType.DISABLED;
import static org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType.SAME;

public class OnManagementPortCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnManagementPort.class.getName());
        if (attributes == null) {
            throw new IllegalStateException("Failed to decide management part!");
        }
        ManagementPortType requiredType = (ManagementPortType) attributes.get("value");
        ManagementPortType actualType = getType(context.getEnvironment());
        if (actualType == requiredType) {
            return new ConditionOutcome(true,
                    "Management port and Restlight server port are binding on same port!");
        }
        return new ConditionOutcome(false,
                "Management port and Restlight server port are binding on different port!");
    }

    private ManagementPortType getType(Environment environment) {
        Integer serverPort = environment.getProperty(SERVER_PORT, Integer.class);
        Integer managementPort = environment.getProperty(MANAGEMENT_SERVER_PORT, Integer.class);
        if (managementPort != null && managementPort < 0) {
            return DISABLED;
        }

        if (managementPort == null) {
            return SAME;
        }

        return (serverPort == null && managementPort.equals(8080))
                || (managementPort != 0 && managementPort.equals(serverPort)) ? SAME : DIFFERENT;
    }

}
