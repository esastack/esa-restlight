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

import java.lang.annotation.*;

/**
 * Indicates that current controller(s) should be scheduled by which {@link esa.restlight.server.schedule.Scheduler}
 * @see esa.restlight.server.schedule.Scheduling
 * @see esa.restlight.server.schedule.Schedulers
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduled {

    /**
     * Specifies which {@link esa.restlight.server.schedule.Scheduler} who's name ({@link
     * esa.restlight.server.schedule.Scheduler#name()}) equals to the value should be use to current component(s).
     *
     * @return {@code ""} means default.
     * @see esa.restlight.server.schedule.Schedulers#IO
     * @see esa.restlight.server.schedule.Schedulers#BIZ
     */
    String value();

}
