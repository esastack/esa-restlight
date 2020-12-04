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
package esa.restlight.core.handler.impl;

import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.HandlerAdvice;
import esa.restlight.core.handler.HandlerAdvicesFactory;
import esa.restlight.core.spi.HandlerAdviceFactory;
import esa.restlight.core.util.OrderedComparator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class HandlerAdvicesFactoryImpl implements HandlerAdvicesFactory {

    private final DeployContext<? extends RestlightOptions> ctx;
    private final Collection<? extends HandlerAdviceFactory> factories;

    public HandlerAdvicesFactoryImpl(DeployContext<? extends RestlightOptions> ctx,
                                     Collection<? extends HandlerAdviceFactory> factories) {
        this.ctx = ctx;
        this.factories = factories;
    }

    @Override
    public HandlerAdvice[] getHandlerAdvices(Handler handler) {
        List<HandlerAdvice> advices = new LinkedList<>();
        if (factories != null && !factories.isEmpty()) {
            for (HandlerAdviceFactory factory : factories) {
                factory.handlerAdvice(ctx, handler).ifPresent(advices::add);
            }
        }
        OrderedComparator.sort(advices);
        return advices.toArray(new HandlerAdvice[0]);
    }
}
