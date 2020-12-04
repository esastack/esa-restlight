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

import esa.restlight.core.util.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnableServerConditionTest {

    @Test
    void test() {
        ServerPortType.reset();
        final EnableServerCondition condition = new EnableServerCondition();
        final ConditionContext context = mock(ConditionContext.class);
        final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

        assertEquals(ServerPortType.DEFINED, ServerPortType.get());
        assertTrue(condition.getMatchOutcome(context, metadata).isMatch());
        ServerPortType.set(ServerPortType.MOCK);
        assertFalse(condition.getMatchOutcome(context, metadata).isMatch());
        ServerPortType.set(ServerPortType.RANDOM);
        final ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        final MutablePropertySources props = mock(MutablePropertySources.class);
        when(context.getEnvironment()).thenReturn(env);
        when(env.getPropertySources()).thenReturn(props);

        assertTrue(condition.getMatchOutcome(context, metadata).isMatch());

        verify(props)
                .addFirst(argThat(p -> Integer.parseInt(
                        Objects.requireNonNull(p.getProperty(Constants.SERVER_PORT)).toString()) > 1024
                        && Integer.parseInt(
                        Objects.requireNonNull(p.getProperty(Constants.MANAGEMENT_SERVER_PORT)).toString()) > 1024));
    }

}
