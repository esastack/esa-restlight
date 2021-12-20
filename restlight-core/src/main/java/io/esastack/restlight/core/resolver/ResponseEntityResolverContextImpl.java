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
package io.esastack.restlight.core.resolver;

import esa.commons.Checks;
import esa.commons.StringUtils;
import esa.commons.spi.SpiLoader;
import io.esastack.restlight.core.spi.ResponseEntityChannelFactory;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.core.util.OrderedComparator;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.impl.AttributesProxy;

import java.util.Collections;
import java.util.List;

public class ResponseEntityResolverContextImpl extends AttributesProxy implements ResponseEntityResolverContext {

    private static final ResponseEntityChannelFactory CHANNEL_FACTORY;

    static {
        List<ResponseEntityChannelFactory> factories = SpiLoader.cached(ResponseEntityChannelFactory.class)
                .getByTags(Collections.singletonMap(Constants.INTERNAL, StringUtils.empty()));
        assert !factories.isEmpty();
        OrderedComparator.sort(factories);
        CHANNEL_FACTORY = factories.get(0);
    }

    private final RequestContext context;
    private final ResponseEntity entity;
    private final ResponseEntityChannel channel;
    private final List<ResponseEntityResolver> resolvers;
    private final ResponseEntityResolverAdvice[] advices;
    private int index;

    public ResponseEntityResolverContextImpl(RequestContext context,
                                             ResponseEntity entity,
                                             List<ResponseEntityResolver> resolvers,
                                             ResponseEntityResolverAdvice[] advices) {
        super(context);
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(resolvers, "resolvers");
        this.channel = CHANNEL_FACTORY.create(context);
        this.context = context;
        this.entity = entity;
        this.resolvers = resolvers;
        this.advices = advices;
    }

    @Override
    public RequestContext context() {
        return context;
    }

    @Override
    public ResponseEntity entityInfo() {
        return entity;
    }

    @Override
    public ResponseEntityChannel channel() {
        return channel;
    }

    @Override
    public void proceed() throws Exception {
        if (advices == null || index >= advices.length) {
            HandledValue<Void> handled;
            for (ResponseEntityResolver resolver : resolvers) {
                handled = resolver.writeTo(entity, channel, context);
                if (handled.isSuccess()) {
                    return;
                }
            }
            return;
        }

        advices[index++].aroundWrite(this);
    }
}

