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
package io.esastack.restlight.jaxrs.spi;

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.config.RestlightOptionsConfigure;
import io.esastack.restlight.core.resolver.entity.response.ResponseEntityResolverFactory;
import io.esastack.restlight.jaxrs.resolver.rspentity.FlexibleResponseEntityResolverFactory;
import io.esastack.restlight.jaxrs.resolver.rspentity.NegotiationResponseResolverFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlexibleResponseEntityResolverProviderTest {

    @Test
    void testFactoryBean() {
        final FlexibleResponseEntityResolverProvider provider = new FlexibleResponseEntityResolverProvider();
        final DeployContext context = mock(DeployContext.class);
        final RestlightOptions options = RestlightOptionsConfigure.defaultOpts();
        when(context.options()).thenReturn(options);

        final Optional<ResponseEntityResolverFactory> rst1 = provider.factoryBean(context);
        assertTrue(rst1.isPresent());
        assertEquals(FlexibleResponseEntityResolverFactory.class, rst1.get().getClass());

        options.getSerialize().getResponse().setNegotiation(true);
        final Optional<ResponseEntityResolverFactory> rst2 = provider.factoryBean(context);
        assertTrue(rst2.isPresent());
        assertEquals(NegotiationResponseResolverFactory.class, rst2.get().getClass());
    }

}

