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
package esa.restlight.spring.util;

import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class SpringContextUtilsTest {

    @Test
    void testGetBean() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        assertFalse(SpringContextUtils.getBean(ctx, RestlightOptions.class).isPresent());

        when(ctx.getBean(RestlightOptions.class)).thenThrow(new BeanCreationException("foo"));
        assertFalse(SpringContextUtils.getBean(ctx, RestlightOptions.class).isPresent());

        final RestlightOptions options = RestlightOptionsConfigure.defaultOpts();
        reset(ctx);
        when(ctx.getBean(RestlightOptions.class)).thenReturn(options);
        assertSame(options, SpringContextUtils.getBean(ctx, RestlightOptions.class).get());
    }

}
