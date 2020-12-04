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
package esa.restlight.springmvc.annotation.shaded;

import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

public class RequestMapping0 {

    private static final Class<?>[] EXTENDED_CLASSES =
            {GetMapping.class, PostMapping.class, PutMapping.class,
                    DeleteMapping.class, PatchMapping.class};

    private final String name;
    private final String[] path;
    private final String[] method;
    private final String[] params;
    private final String[] headers;
    private final String[] consumes;
    private final String[] produces;

    public RequestMapping0(String name,
                           String[] path,
                           String[] method,
                           String[] params,
                           String[] headers,
                           String[] consumes,
                           String[] produces) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(consumes, "consumes");
        Objects.requireNonNull(produces, "produces");
        this.name = name;
        this.path = path;
        this.method = method;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    public static Class<? extends Annotation> shadedClass() {
        return RequestMapping.class;
    }

    @SuppressWarnings("unchecked")
    public static Class<Annotation>[] extendedClasses() {
        return (Class<Annotation>[]) EXTENDED_CLASSES;
    }

    public static RequestMapping0 fromShade(Annotation ann) {
        if (ann == null) {
            return null;
        }
        if (ann instanceof RequestMapping) {
            RequestMapping instance = (RequestMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    Arrays.stream(instance.method())
                            .map(RequestMethod::name)
                            .toArray(String[]::new),
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        } else if (ann instanceof GetMapping) {
            GetMapping instance = (GetMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    new String[]{RequestMethod.GET.name()},
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        } else if (ann instanceof PostMapping) {
            PostMapping instance = (PostMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    new String[]{RequestMethod.POST.name()},
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        } else if (ann instanceof PutMapping) {
            PutMapping instance = (PutMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    new String[]{RequestMethod.PUT.name()},
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        } else if (ann instanceof DeleteMapping) {
            DeleteMapping instance = (DeleteMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    new String[]{RequestMethod.DELETE.name()},
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        } else if (ann instanceof PatchMapping) {
            PatchMapping instance = (PatchMapping) ann;
            return new RequestMapping0(instance.name(),
                    AliasUtils.getStringArrayFromValueAlias(instance.path(), instance.value(), "path"),
                    new String[]{RequestMethod.PATCH.name()},
                    instance.params(),
                    instance.headers(),
                    instance.consumes(),
                    instance.produces());
        }
        throw new IllegalArgumentException("Annotation type mismatch");
    }

    public String name() {
        return name;
    }

    public String[] path() {
        return path;
    }

    public String[] value() {
        return path();
    }

    public String[] method() {
        return method;
    }

    public String[] params() {
        return params;
    }

    public String[] headers() {
        return headers;
    }

    public String[] consumes() {
        return consumes;
    }

    public String[] produces() {
        return produces;
    }
}
