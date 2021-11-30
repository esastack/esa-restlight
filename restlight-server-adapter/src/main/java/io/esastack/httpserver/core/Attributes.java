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
package io.esastack.httpserver.core;

public interface Attributes {

    /**
     * Returns the value of the named attribute as an Object, or{@code null} if no attribute of the given name exists.
     *
     * @param name name
     * @return value
     */
    Object getAttribute(String name);

    /**
     * Stores an attribute in this request. Attributes are reset between requests.
     *
     * @param name  name
     * @param value value
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the value of the named attribute as an Object and cast to target type, or{@code null} if no attribute of
     * the given name exists.
     *
     * @param name name
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T getUncheckedAttribute(String name) {
        return (T) getAttribute(name);
    }

    /**
     * Removes an attribute from this request. This method is not generally needed as attributes only persist as long as
     * the request is being handled.
     *
     * @param name name
     * @return value
     */
    Object removeAttribute(String name);

    /**
     * Removes an attribute from this request and cast to target type. This method is not generally needed as attributes
     * only persist as long as the request is being handled.
     *
     * @param name name
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T removeUncheckedAttribute(String name) {
        return (T) removeAttribute(name);
    }

    /**
     * Check whether the given attribute is present in current request.
     *
     * @param name name
     * @return {@code true} if the value of given attribute name is present, otherwise {@code false}
     */
    default boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    /**
     * Returns an {@code String[]} containing the names of the attributes available to this request
     *
     * @return names
     */
    String[] attributeNames();

}

