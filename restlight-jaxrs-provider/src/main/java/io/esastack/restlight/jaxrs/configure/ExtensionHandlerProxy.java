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
package io.esastack.restlight.jaxrs.configure;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class ExtensionHandlerProxy {

    static Object newProxy(Class<?> clazz, InvocationHandler handler) {
        Class<?>[] interfaces = { clazz };
        ClassLoader loader;
        final SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            loader = clazz.getClassLoader();
        } else {
            loader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>) clazz::getClassLoader);
        }

        return Proxy.newProxyInstance(loader, interfaces, handler);
    }

    private ExtensionHandlerProxy() {
    }
}

