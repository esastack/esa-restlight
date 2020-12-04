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

import esa.restlight.core.serialize.HttpRequestSerializer;

import java.lang.annotation.*;

/**
 * Indicates that the request content(header, body...) of the annotated interface('s) should be deserialize by the
 * given implementation of {@link HttpRequestSerializer} if necessary(maybe there'e no need to deserialize such as a
 * parameter in the url.).
 * <p>
 * It could be used to associate with the @RequestBody(if in spring environment).
 * <p>
 * {@link RequestSerializer} could be annotated on the parameter, method and the class, and also the priority of the 3
 * places will be parameter(highest) &gt; method &gt; class(lowest).
 */
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestSerializer {

    /**
     * Specify the {@link HttpRequestSerializer}.
     *
     * @return class type of {@link HttpRequestSerializer}.
     */
    Class<? extends HttpRequestSerializer> value();
}
