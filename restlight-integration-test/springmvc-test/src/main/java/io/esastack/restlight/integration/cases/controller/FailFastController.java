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

import io.esastack.restlight.core.annotation.Scheduled;
import io.esastack.restlight.spring.util.RestlightBizExecutorAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

/**
 * @author chenglu
 */
@RestController
@RequestMapping("/failfast/")
public class FailFastController implements RestlightBizExecutorAware {

    @Override
    public void setRestlightBizExecutor(Executor bizExecutor) {

    }

    @GetMapping("get/queued")
    @Scheduled("fail-fast-queued")
    public String queued() {
        return Thread.currentThread().getName();
    }

    @PostMapping("post/ttfb")
    @Scheduled("fail-fast-ttfb")
    public String ttfb() {
        return Thread.currentThread().getName();
    }
}
