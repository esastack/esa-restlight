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
package io.esastack.restlight.core.util;

import esa.commons.StringUtils;
import io.esastack.commons.net.http.HttpHeaderNames;
import io.esastack.commons.net.http.HttpHeaders;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static io.esastack.commons.net.http.HttpHeaderNames.ACCEPT_ENCODING;
import static io.esastack.commons.net.http.HttpHeaderNames.ACCEPT_LANGUAGE;

public final class HttpHeaderUtils {

    public static List<String> getAcceptEncodings(HttpHeaders headers) {
        if (headers == null) {
            return Collections.emptyList();
        }

        final List<String> encodings = new LinkedList<>();
        headers.getAll(ACCEPT_ENCODING).forEach(encoding -> {
            if (StringUtils.isNotEmpty(encoding)) {
                String[] items = encoding.split(",");

                for (String item : items) {
                    if (StringUtils.isNotEmpty(item)) {
                        encodings.add(parseToEncoding(item.trim()));
                    }
                }
            }
        });

        return encodings;
    }

    public static List<Locale> getAcceptLanguages(HttpHeaders headers) {
        if (headers == null) {
            return Collections.emptyList();
        }

        final List<Locale> languages = new LinkedList<>();
        headers.getAll(ACCEPT_LANGUAGE).forEach(language -> {
            if (StringUtils.isNotEmpty(language)) {
                String[] items = language.split(",");
                for (String item : items) {
                    if (StringUtils.isNotEmpty(item)) {
                        languages.add(parseToLanguage(item.trim()));
                    }
                }
            }
        });

        return languages;
    }

    public static Locale getLanguage(HttpHeaders headers) {
        String language = headers.get(HttpHeaderNames.CONTENT_LANGUAGE);
        if (StringUtils.isEmpty(language)) {
            return null;
        }
        return new Locale(language);
    }

    public static String parseToEncoding(String target) {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        return target.split(";")[0].trim();
    }

    public static Locale parseToLanguage(String target) {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        String language = target.split(";")[0].trim();
        String[] values = language.split("-");
        if (values.length == 1) {
            return new Locale(values[0].trim());
        } else {
            return new Locale(values[0].trim(), values[1].trim());
        }
    }

    public static String concatHeaderValues(List<String> values) {
        if (values == null) {
            return null;
        } else if (values.isEmpty()) {
            return StringUtils.EMPTY_STRING;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String value : values) {
            if (!first) {
                sb.append(",");
            }
            sb.append(value);
            first = false;
        }
        return sb.toString();
    }

    private HttpHeaderUtils() {
    }
}
