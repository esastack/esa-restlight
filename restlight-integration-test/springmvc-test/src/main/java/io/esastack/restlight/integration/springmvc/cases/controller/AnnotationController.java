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

package io.esastack.restlight.integration.springmvc.cases.controller;

import io.esastack.restlight.core.annotation.QueryBean;
import io.esastack.restlight.core.annotation.RequestBean;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomRequestBean;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomRequestBody;
import io.esastack.restlight.integration.springmvc.cases.annotation.CustomResponseBody;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotation/")
public class AnnotationController {

    @GetMapping("get")
    public UserData get(@RequestParam String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @PostMapping("post")
    public UserData post(@RequestBody UserData user) {
        return user;
    }

    @DeleteMapping("delete/{name}")
    public UserData delete(@PathVariable("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @PutMapping("put")
    public UserData put(@CookieValue("name") String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GetMapping("get/querybean")
    public UserData queryBean(@QueryBean UserData user) {
        return user;
    }

    @GetMapping("get/requestbean")
    public UserData requestBean(@RequestBean UserData user) {
        return user;
    }

    @GetMapping("get/header")
    public UserData header(@RequestHeader String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GetMapping("get/matrix")
    public UserData matrix(@MatrixVariable MultiValueMap<String, String> map) {
        return UserData.Builder.anUserData()
                .name(map.getFirst("name")).build();
    }

    @GetMapping("get/attribute")
    public UserData attribute(@RequestAttribute String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GetMapping("get/responsestatus")
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public UserData responseStatus() {
        return UserData.Builder.anUserData().build();
    }

    @GetMapping("get/custom/bean")
    public UserData customBean(@CustomRequestBean UserData user) {
        return user;
    }

    @PostMapping("post/custom/body")
    public UserData customBody(@CustomRequestBody UserData user) {
        return user;
    }

    @GetMapping("get/custom/responsebody")
    @CustomResponseBody
    public String customResponseBody(@RequestParam String name) {
        return name;
    }
}
