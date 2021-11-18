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
package io.esastack.restlight.jaxrs.impl.core;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Variant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class VariantListBuilderImpl extends Variant.VariantListBuilder {

    private final List<Variant> variants = new LinkedList<>();
    private final List<Locale> languages = new LinkedList<>();
    private final List<String> encodings = new LinkedList<>();
    private final List<MediaType> mediaTypes = new LinkedList<>();

    @Override
    public List<Variant> build() {
        add();
        List<Variant> newVariants = new ArrayList<>(variants);
        reset();
        return newVariants;
    }

    @Override
    public Variant.VariantListBuilder add() {
        if (languages.isEmpty() || encodings.isEmpty() || mediaTypes.isEmpty()) {
            return this;
        }
        for (final Locale language : languages) {
            for (final String encoding : encodings) {
                for (MediaType mediaType : mediaTypes) {
                    variants.add(new Variant(mediaType, language, encoding));
                }
            }
        }
        return this;
    }

    @Override
    public Variant.VariantListBuilder languages(Locale... languages) {
        if (languages == null || languages.length == 0) {
            return this;
        }
        this.languages.addAll(Arrays.asList(languages));
        return this;
    }

    @Override
    public Variant.VariantListBuilder encodings(String... encodings) {
        if (encodings == null || encodings.length == 0) {
            return this;
        }
        this.encodings.addAll(Arrays.asList(encodings));
        return this;
    }

    @Override
    public Variant.VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        if (mediaTypes == null || mediaTypes.length == 0) {
            return this;
        }
        this.mediaTypes.addAll(Arrays.asList(mediaTypes));
        return this;
    }

    private void reset() {
        variants.clear();
        languages.clear();
        encodings.clear();
        mediaTypes.clear();
    }
}

