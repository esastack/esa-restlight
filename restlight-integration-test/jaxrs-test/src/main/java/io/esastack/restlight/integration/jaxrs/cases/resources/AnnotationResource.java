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

import io.esastack.restlight.integration.jaxrs.entity.UserData;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import org.springframework.stereotype.Controller;

@Path("/annotation/")
@Controller
public class AnnotationResource {

    @GET
    @Path("get")
    public UserData get(@QueryParam("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @POST
    @Path("post")
    public UserData post(UserData userData) {
        return userData;
    }

    @DELETE
    @Path("delete")
    public UserData delete(@BeanParam UserData userData) {
        return userData;
    }

    @PUT
    @Path("/put/{name}")
    public UserData put(@PathParam("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GET
    @Path("get/matrix/{age}")
    public UserData matrix(@MatrixParam("name") String name, @PathParam("age") Integer age) {
        UserData userData = UserData.Builder.anUserData()
                .name(name).build();
        userData.setAge(age);
        return userData;
    }

    @GET
    @Path("get/context/headers")
    public UserData headers(@Context HttpHeaders httpHeaders) {
        return UserData.Builder.anUserData()
                .name(httpHeaders.getHeaderString("name")).build();
    }

    @GET
    @Path("get/context/providers")
    public String providers(@Context Providers providers) {
        return providers.getClass().getName();
    }

    @GET
    @Path("get/context/request")
    public String request(@Context Request request) {
        return request.getMethod();
    }

    @GET
    @Path("get/context/resource")
    public String resource(@Context ResourceContext resourceContext) {
        return resourceContext.getResource(this.getClass()).getClass().getName();
    }

    @GET
    @Path("get/context/uri")
    public String uri(@Context UriInfo uriInfo) {
        return uriInfo.getRequestUri().getPath();
    }

    @GET
    @Path("get/context/configuration")
    public String configuration(@Context Configuration configuration) {
        return configuration.getClass().getName();
    }
}
