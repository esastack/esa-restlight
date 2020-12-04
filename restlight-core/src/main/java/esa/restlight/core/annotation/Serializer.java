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

import esa.restlight.core.serialize.HttpBodySerializer;

import java.lang.annotation.*;

/**
 * Indicates that the request/response content(header, body...) of the annotated interface('s) should be
 * serialize/deserialize by the
 * given implementation of {@link HttpBodySerializer} if necessary(maybe there'e no need to deserialize such as a
 * parameter in the url.).
 *
 * It could be used to associate with the @RequestBody/@ResponseBody(if in spring environment).
 *
 * {@link Serializer} could be annotated on the parameter, method and the class, and also the priority of the
 * 3 places will be method(highest) &gt; class(lowest).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Serializer {

    Class<? extends HttpBodySerializer> value();
}
