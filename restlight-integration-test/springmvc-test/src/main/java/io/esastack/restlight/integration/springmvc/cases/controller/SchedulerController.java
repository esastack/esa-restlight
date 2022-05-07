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

import io.esastack.restlight.core.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.esastack.restlight.core.server.processor.schedule.Schedulers.IO;

@RestController
@RequestMapping("/scheduler/")
public class SchedulerController {

    @GetMapping("biz")
    public String biz() {
        return Thread.currentThread().getName();
    }

    @GetMapping("io")
    @Scheduled(IO)
    public String io() {
        return Thread.currentThread().getName();
    }

    @GetMapping("custom")
    @Scheduled("custom")
    public String custom() {
        return Thread.currentThread().getName();
    }
}
