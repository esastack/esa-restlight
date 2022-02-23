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
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariantListBuilderImplTest {

    @Test
    void testBasic() {
        final VariantListBuilderImpl builder = new VariantListBuilderImpl();
        builder.languages(null);
        builder.languages(new Locale[0]);
        builder.encodings(null);
        builder.encodings(new String[0]);
        builder.mediaTypes(null);
        builder.mediaTypes(new MediaType[0]);
        assertEquals(0, builder.build().size());

        builder.languages(Locale.SIMPLIFIED_CHINESE);
        builder.encodings("gzip");
        builder.mediaTypes(MediaType.APPLICATION_JSON_TYPE);
        assertEquals(1, builder.add().build().size());
        assertEquals(Locale.SIMPLIFIED_CHINESE, builder.build().get(0).getLanguage());
        assertEquals("gzip", builder.build().get(0).getEncoding());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, builder.build().get(0).getMediaType());

        assertEquals(1, builder.build().size());
    }

}

