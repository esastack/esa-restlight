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
package io.esastack.restlight.jaxrs.util;

import esa.commons.StringUtils;

public final class UriUtils {

    /**
     * Encode given {@code target}.
     *
     * @param target                 target
     * @param encodeSlashInPath      If {@code true}, the slash ({@code '/'}) characters in template values will
     *                               be encoded, otherwise not.
     * @param encodePercentAnyway    All {@code '%'} characters in the {@code target} values will be encoded.
     * @param encodePercentCondition If {@code true}, all {@code '%'} characters in the {@code target} values
     *                               that are not followed by two hexadecimal numbers will be encoded, otherwise not.
     * @return encoded value
     */
    public static String encode(String target, boolean encodeSlashInPath,
                                boolean encodePercentAnyway, boolean encodePercentCondition) {
        if (StringUtils.isEmpty(target)) {
            return target;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            char current = target.charAt(i);
            if ('/' == current) {
                if (encodeSlashInPath) {
                    sb.append("%2F");
                } else {
                    sb.append(current);
                }
            } else if ('%' == current) {
                if (encodePercentAnyway) {
                    sb.append("%25");
                } else if (encodePercentCondition
                        && (i + 1 < target.length() && isHex(target.charAt(i + 1)))
                        && (i + 2 < target.length() && isHex(target.charAt(i + 2)))) {
                    sb.append("%25");
                } else {
                    sb.append(current);
                }
            } else {
                sb.append(current);
            }
        }

        return sb.toString();
    }

    public static boolean isHex(char target) {
        if (target >= '0' && target <= '9') {
            return true;
        }
        if (target >= 'A' && target <= 'F') {
            return true;
        }
        return target >= 'a' && target <= 'f';
    }

    private UriUtils() {
    }
}
