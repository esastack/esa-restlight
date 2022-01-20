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
package io.esastack.restlight.ext.interceptor.signature;

import esa.commons.ClassUtils;
import io.esastack.restlight.core.DeployContextImpl;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.method.RouteHandlerMethodImpl;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.ext.interceptor.annotation.IgnoreSignValidation;
import io.esastack.restlight.ext.interceptor.annotation.SignValidation;
import io.esastack.restlight.ext.interceptor.config.SignatureOptionsConfigure;
import io.esastack.restlight.server.route.Mapping;
import io.esastack.restlight.server.route.Routing;
import io.esastack.restlight.server.schedule.Schedulers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static io.esastack.restlight.ext.interceptor.signature.AbstractSignatureRouteInterceptor.SIGN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSha1SignatureRouteInterceptorTest extends AbstractSignatureInterceptorTest {

    private final SignValidationBean bean = new SignValidationBean();

    private final HmacSha1SignatureRouteInterceptor signInterceptor = new HmacSha1SignatureRouteInterceptor(
            SignatureOptionsConfigure.newOpts()
                    .appId(APP_ID_PARAM_NAME)
                    .secretVersion(SECRET_VERSION_PARAM_NAME)
                    .timestamp(TIMESTAMP_PARAM_NAME)
                    .signature(SIGNATURE_PARAM)
                    .expireSeconds(TIMESTAMP_ACTIVE_SECONDS)
                    .configured(),
            (appId, secretVersion, timestamp) -> "");

    @Test
    void testWhenSignatureAll() throws NoSuchMethodException {
        final Context context = buildContext(true);
        Routing route0 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method0"));
        assertTrue(signInterceptor.match(context, route0));

        Routing route1 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method1"));
        assertFalse(signInterceptor.match(context, route1));

        Routing route2 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method2"));
        assertFalse(signInterceptor.match(context, route2));

        Routing route3 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method3"));
        assertFalse(signInterceptor.match(context, route3));
    }

    @Test
    void testWhenSignatureNone() throws NoSuchMethodException {
        final Context context = buildContext(false);
        Routing route0 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method0"));
        assertTrue(signInterceptor.match(context, route0));

        Routing route1 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method1"));
        assertFalse(signInterceptor.match(context, route1));

        Routing route2 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method2"));
        assertTrue(signInterceptor.match(context, route2));

        Routing route3 = buildRouting(bean, SignValidationBean.class.getDeclaredMethod("method3"));
        assertTrue(signInterceptor.match(context, route3));
    }

    private Routing buildRouting(Object obj, Method method) {
        return new Routing() {
            @Override
            public Mapping mapping() {
                return null;
            }

            @Override
            public Optional<Object> handler() {
                return Optional.of(RouteHandlerMethodImpl.of(ClassUtils.getUserType(obj), method,
                        false, Schedulers.BIZ));
            }
        };
    }

    private Context buildContext(boolean verifyAll) {
        return new Context(new RestlightOptions().extOption(SIGN + ".verify-all", String.valueOf(verifyAll)));
    }

    private static class Context extends DeployContextImpl {

        protected Context(RestlightOptions options) {
            super(Constants.SERVER, options);
        }

    }

    private static class SignValidationBean {

        @SignValidation
        private void method0() {

        }

        @IgnoreSignValidation
        private void method1() {

        }

        @SignValidation
        @IgnoreSignValidation
        private void method2() {

        }

        @IgnoreSignValidation
        @SignValidation
        private void method3() {

        }
    }
}
