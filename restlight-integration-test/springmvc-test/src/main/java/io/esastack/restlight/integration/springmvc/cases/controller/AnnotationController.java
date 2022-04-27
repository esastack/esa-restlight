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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("get/matrix/{age}")
    public UserData matrix(@PathVariable Integer age, @MatrixVariable String name) {
        return UserData.Builder.anUserData()
                .age(age).name(name).build();
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
