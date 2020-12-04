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
package esa.restlight.core.mock;

import esa.restlight.core.DeployContextImpl;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.config.RestlightOptionsConfigure;
import esa.restlight.core.resolver.HandlerResolverFactory;
import esa.restlight.core.util.Constants;

import java.util.Optional;

public class MockContext extends DeployContextImpl<RestlightOptions> {

    protected MockContext(String name, RestlightOptions options) {
        super(name, options);
    }

    public static MockContext mock() {
        return new MockContext(Constants.SERVER, RestlightOptionsConfigure.defaultOpts());
    }

    public static MockContext withHandlerResolverFactory(HandlerResolverFactory factory) {
        return new MockContext(Constants.SERVER, RestlightOptionsConfigure.defaultOpts()) {
            @Override
            public Optional<HandlerResolverFactory> resolverFactory() {
                return Optional.of(factory);
            }
        };
    }
}
