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
package io.esastack.restlight.core.util;

import esa.commons.ClassUtils;
import esa.commons.ExceptionUtils;
import esa.commons.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class ResourceUtils {

    /**
     * Gets the resource {@link File} by given {@code path}. It will try to get the resource from the class path if the
     * given {@code path} is a pattern of 'classpath:xxxx'.
     *
     * @param path resource path
     * @return resource file
     */
    public static File getFile(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        if (path.startsWith("classpath:")) {
            String p = path.substring("classpath:".length());
            ClassLoader cl = ClassUtils.getClassLoader();
            final URL url = (cl != null ? cl.getResource(p) : ClassLoader.getSystemResource(p));
            if (url == null || !"file".equals(url.getProtocol())) {
                ExceptionUtils.throwException(new FileNotFoundException(p));
                return null;
            }
            try {
                return new File(new URI(url.toString().replace(" ", "%20"))
                        .getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                return new File(url.getFile());
            }
        }
        return new File(path);
    }

    private ResourceUtils() {
    }
}
