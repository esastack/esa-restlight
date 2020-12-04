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
package esa.restlight.ext.interceptor.signature;

import esa.restlight.core.interceptor.HandlerInterceptor;

public interface SignValidationScope {

    /**
     * Obtain the url path corresponding to the method which needed to verify signature.
     *
     * @return url paths which are forced to verify signature.
     * @see esa.restlight.core.interceptor.HandlerInterceptor#includes()
     */
    default String[] includes() {
        return null;
    }

    /**
     * Obtain the url path corresponding to the method which are needn't to verify signature.
     *
     * @return url paths which are forced not to verify signature.
     * @see HandlerInterceptor#excludes()
     */
    default String[] excludes() {
        return null;
    }

}
