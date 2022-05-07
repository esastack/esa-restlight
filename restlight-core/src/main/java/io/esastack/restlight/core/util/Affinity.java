/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.core.util;

/**
 * Indicates the affinity value between current component and the target subject.
 * <p>
 * Here are the cases of {@link #affinity()}'s return value:
 * <ul>
 * <li>{@code 0}: current component is always attaching with the target subject.</li>
 * <li>Positive value: current component has a positive affinity with the target subject. The larger the value,
 * the stronger the affinity</li>
 * <li>Negative value: current component has no affinity with the target subject.</li>
 * </ul>
 */
public interface Affinity {

    /**
     * Current component is always attaching with the target subject.
     */
    int ATTACHED = 0;

    /**
     * Current component has a high affinity with the target subject this is the highest value.
     */
    int HIGHEST = 1;
    /**
     * Current component has no affinity with the target subject.
     */
    int DETACHED = -1;

    /**
     * Gets the affinity value.
     *
     * @return affinity.
     */
    int affinity();
}
