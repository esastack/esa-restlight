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

import io.esastack.restlight.integration.jaxrs.cases.annotation.Filter;
import io.esastack.restlight.integration.jaxrs.entity.UserData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import org.springframework.stereotype.Controller;

@Controller
@Path("/filter/")
public class FilterResource {

    @GET
    @Path("request/global")
    public UserData requestGlobal(@HeaderParam("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GET
    @Path("response/global")
    public UserData responseGlobal() {
        return UserData.Builder.anUserData().build();
    }

    @GET
    @Path("request/binding")
    @Filter
    public UserData requestBinding(@HeaderParam("name") String name) {
        return UserData.Builder.anUserData().name(name).build();
    }

    @GET
    @Path("response/binding")
    @Filter
    public UserData responseBinding() {
        return UserData.Builder.anUserData().build();
    }

    @GET
    @Path("dynamic/feature")
    public UserData dynamicFeature(@Context Configuration configuration) {
        return UserData.Builder.anUserData()
                .name(configuration.getProperty("name").toString()).build();
    }
}
