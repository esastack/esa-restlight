/*
 * Copyright 2022 OPPO ESA Stack Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.esastack.restlight.integration.jaxrs.cases.resources;

import io.esastack.restlight.integration.jaxrs.entity.MessageBodyData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.springframework.stereotype.Controller;

@Controller
@Path("/message/body/")
public class MessageBodyResource {

    @GET
    @Path("reader")
    public MessageBodyData reader(MessageBodyData messageBodyData) {
        return messageBodyData;
    }

    @GET
    @Path("writer")
    public MessageBodyData writer() {
        return null;
    }
}
