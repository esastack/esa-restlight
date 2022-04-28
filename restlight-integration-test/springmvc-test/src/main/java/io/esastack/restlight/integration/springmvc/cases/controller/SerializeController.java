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

import io.esastack.restlight.core.annotation.Serializer;
import io.esastack.restlight.core.serialize.GsonHttpBodySerializer;
import io.esastack.restlight.integration.springmvc.entity.UserData;
import io.esastack.restlight.integration.springmvc.entity.UserProtobufData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/serialize/")
public class SerializeController {

    @PostMapping("json")
    @Serializer(GsonHttpBodySerializer.class)
    public UserData json(@RequestBody UserData user) {
        return user;
    }


    @PostMapping("protobuf")
    public UserProtobufData.Data protobuf(@RequestBody UserProtobufData.Data user) {
        return user;
    }
}
