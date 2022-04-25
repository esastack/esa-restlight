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

package io.esastack.restlight.integration.cases.controller;

import io.esastack.restlight.integration.entity.UserData;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/advice/")
public class AdviceController {


    @GetMapping("get/param/factory")
    public UserData customParamAdviceByFactory(@RequestParam String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GetMapping("get/param/adaptor")
    public UserData customParamAdviceByAdaptor(@RequestParam String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @PostMapping("post/entity/factory")
    public UserData customEntityAdviceByFactory(@RequestBody UserData user) {
        return user;
    }

    @PostMapping("post/entity/adaptor")
    public UserData customEntityAdviceByAdaptor(@RequestBody UserData user) {
        return user;
    }

    @GetMapping("get/response/entity/factory")
    public UserData customResponseEntityAdviceByFactory(@RequestParam String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }

    @GetMapping("get/response/entity/adaptor")
    public UserData customResponseEntityAdviceByAdaptor(@RequestParam String name) {
        return UserData.Builder.anUserData()
                .name(name).build();
    }
}
