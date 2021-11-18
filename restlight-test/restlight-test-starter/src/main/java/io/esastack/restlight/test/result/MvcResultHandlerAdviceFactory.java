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
package io.esastack.restlight.test.result;

import esa.commons.spi.Feature;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.handler.Handler;
import io.esastack.restlight.core.handler.HandlerAdvice;
import io.esastack.restlight.core.spi.HandlerAdviceFactory;
import io.esastack.restlight.core.util.Constants;

import java.util.Optional;

@Feature(tags = Constants.INTERNAL)
public class MvcResultHandlerAdviceFactory implements HandlerAdviceFactory {

    @Override
    public Optional<HandlerAdvice> handlerAdvice(DeployContext<? extends RestlightOptions> ctx, Handler handler) {
        return Optional.of(new MvcResultHandlerAdvice());
    }

}
