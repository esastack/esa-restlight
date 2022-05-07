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
package io.esastack.restlight.jaxrs.spi;

import esa.commons.annotation.Internal;
import esa.commons.spi.Feature;
import io.esastack.restlight.core.handler.method.Param;
import io.esastack.restlight.core.handler.method.ResolvableParamPredicate;
import io.esastack.restlight.core.util.Constants;
import io.esastack.restlight.jaxrs.util.JaxrsUtils;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

@Internal
@Feature(tags = Constants.INTERNAL)
public class JaxrsResolvableParamPredicate implements ResolvableParamPredicate {

    @Override
    public boolean test(Param param) {
        if (param.isMethodParam()) {
            return true;
        }
        if (isInRootResource(param)) {
            return param.hasAnnotation(Context.class)
                    || param.hasAnnotation(BeanParam.class)
                    || param.hasAnnotation(CookieParam.class)
                    || param.hasAnnotation(FormParam.class)
                    || param.hasAnnotation(HeaderParam.class)
                    || param.hasAnnotation(MatrixParam.class)
                    || param.hasAnnotation(PathParam.class)
                    || param.hasAnnotation(QueryParam.class);
        } else {
            return param.hasAnnotation(Context.class);
        }
    }

    private static boolean isInRootResource(Param param) {
        Class<?> declaringClassType;
        if (param.isConstructorParam()) {
            declaringClassType = param.constructorParam().declaringClass();
        } else {
            declaringClassType = param.fieldParam().declaringClass();
        }
        return JaxrsUtils.isRootResource(declaringClassType);
    }

}

