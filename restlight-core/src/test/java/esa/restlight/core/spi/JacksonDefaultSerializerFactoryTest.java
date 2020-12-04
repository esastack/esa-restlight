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
package esa.restlight.core.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import esa.restlight.core.DeployContext;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.mock.MockContext;
import esa.restlight.core.serialize.HttpRequestSerializer;
import esa.restlight.core.serialize.HttpResponseSerializer;
import esa.restlight.core.serialize.JacksonHttpBodySerializer;
import esa.restlight.core.spi.impl.JacksonDefaultSerializerFactory;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JacksonDefaultSerializerFactoryTest {

    private final JacksonDefaultSerializerFactory factory = new JacksonDefaultSerializerFactory();

    @Test
    void testSingleton() {
        final HttpRequestSerializer rx = factory.defaultRequestSerializer(MockContext.mock());
        assertNotNull(rx);
        assertTrue(rx instanceof JacksonHttpBodySerializer);
        assertSame(rx, factory.defaultRequestSerializer(MockContext.mock()));
        assertSame(rx, factory.defaultResponseSerializer(MockContext.mock()));
        assertSame(rx, factory.defaultResponseSerializer(MockContext.mock()));
    }

    @Test
    void testUseObjectMapperFromContext() throws Exception {
        final DeployContext<RestlightOptions> ctx = MockContext.mock();
        final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        ctx.attribute(JacksonDefaultSerializerFactory.OBJECT_MAPPER,
                new ObjectMapper().setDateFormat(fmt));
        final HttpResponseSerializer tx =
                factory.defaultResponseSerializer(ctx);
        final Pojo pojo = new Pojo();
        assertEquals("{\"date\":\"" + fmt.format(pojo.date) + "\"}",
                new String(tx.serialize(pojo)));
    }

    private static class Pojo {
        private Date date = new Date();

        public Date getDate() {
            return date;
        }

        void setDate(Date date) {
            this.date = date;
        }
    }
}
