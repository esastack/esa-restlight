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
package io.esastack.restlight.test.context;

import io.esastack.restlight.starter.condition.ServerPortType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class MockServerStarterListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) {
        SpringBootTest.WebEnvironment environment = getEnvironment(testContext);
        if (environment != null) {
            if (!environment.isEmbedded()) {
                ServerPortType.set(ServerPortType.MOCK);
            } else {
                if (environment == SpringBootTest.WebEnvironment.DEFINED_PORT) {
                    ServerPortType.set(ServerPortType.DEFINED);
                } else if (environment == SpringBootTest.WebEnvironment.RANDOM_PORT) {
                    ServerPortType.set(ServerPortType.RANDOM);
                }
            }
        } else {
            ServerPortType.set(ServerPortType.MOCK);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        ServerPortType.reset();
    }

    private SpringBootTest.WebEnvironment getEnvironment(TestContext testContext) {
        SpringBootTest annotation = AnnotatedElementUtils.getMergedAnnotation(testContext.getTestClass(),
                SpringBootTest.class);
        return (annotation != null) ? annotation.webEnvironment() : null;
    }
}
