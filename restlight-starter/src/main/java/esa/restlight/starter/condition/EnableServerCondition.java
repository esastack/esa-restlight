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
package esa.restlight.starter.condition;

import esa.commons.NetworkUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Properties;

import static esa.restlight.core.util.Constants.MANAGEMENT_SERVER_PORT;
import static esa.restlight.core.util.Constants.SERVER_PORT;

public class EnableServerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final ServerPortType type = ServerPortType.get();
        if (ServerPortType.MOCK == type) {
            return new ConditionOutcome(false, "Current server is mocked!");
        } else {
            if (ServerPortType.RANDOM == type) {
                setRandomPort(context.getEnvironment());
            }
            return new ConditionOutcome(true, "Server port is configured!");
        }
    }

    private void setRandomPort(Environment env) {
        if (env instanceof ConfigurableEnvironment) {
            int selectedPort;
            int port;
            while (true) {
                port = NetworkUtils.selectRandomPort();
                if (port > 1024) {
                    selectedPort = port;
                    break;
                }
            }
            Properties props = new Properties();
            props.put(SERVER_PORT, selectedPort);
            props.put(MANAGEMENT_SERVER_PORT, selectedPort);
            ((ConfigurableEnvironment) env).getPropertySources().addFirst(
                    new PropertiesPropertySource("restlight_server_properties", props));
        }
    }
}
