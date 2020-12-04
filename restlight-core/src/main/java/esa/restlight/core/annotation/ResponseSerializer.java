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
package esa.restlight.core.annotation;

import esa.restlight.core.serialize.HttpResponseSerializer;

import java.lang.annotation.*;

/**
 * Indicates that the response body of the annotated interface('s) should be serialize by the given implementation of
 * {@link HttpResponseSerializer}.
 * <p>
 * It could be used to associate with the @ResponseBody(if in spring environment).
 * <p>
 * {@link ResponseSerializer} could be annotated on the method and the class, and also the priority of the 2 places
 * will be method(highest) &gt; class(lowest).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseSerializer {

    /**
     * Specify the {@link HttpResponseSerializer}.
     *
     * @return class type of {@link HttpResponseSerializer}.
     */
    Class<? extends HttpResponseSerializer> value();
}
