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
package io.esastack.restlight.jaxrs.impl.container;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ResponseContainerContextTest {

    @Test
    void testBasic() {
        assertThrows(NullPointerException.class, () -> new ResponseContainerContext(null));
        assertDoesNotThrow(() -> new ResponseContainerContext(mock(AbstractContainerRequestContext.class)));

        final AbstractContainerRequestContext context = mock(AbstractContainerRequestContext.class);
        final ResponseContainerContext delegating = new ResponseContainerContext(context);
        assertThrows(IllegalStateException.class, () -> delegating.abortWith(null));
        assertThrows(IllegalStateException.class, () -> delegating.setRequestUri(null));
        assertThrows(IllegalStateException.class, () -> delegating.setRequestUri(null, null));
        assertThrows(IllegalStateException.class, () -> delegating.setMethod(null));
        assertThrows(IllegalStateException.class, () -> delegating.setEntityStream(null));
    }

}

