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

import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.server.bootstrap.RestlightServer;
import io.esastack.restlight.spring.util.RestlightBizExecutorAware;
import io.esastack.restlight.spring.util.RestlightDeployContextAware;
import io.esastack.restlight.spring.util.RestlightIoExecutorAware;
import io.esastack.restlight.spring.util.RestlightServerAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

/**
 * @author chenglu
 */
@RestController
@RequestMapping("/aware/")
public class AwareController implements RestlightBizExecutorAware, RestlightIoExecutorAware,
        RestlightServerAware, RestlightDeployContextAware {

    private Executor bizExecutor;

    private Executor ioExecutor;

    private RestlightServer server;

    private DeployContext ctx;

    @Override
    public void setRestlightBizExecutor(Executor bizExecutor) {
        this.bizExecutor = bizExecutor;
    }

    @Override
    public void setRestlightIoExecutor(Executor ioExecutor) {
        this.ioExecutor = ioExecutor;
    }

    @Override
    public void setRestlightServer(RestlightServer server) {
        this.server = server;
    }

    @Override
    public void setDeployContext(DeployContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping("get/biz")
    public String bizAware() {
        return bizExecutor.getClass().getName();
    }

    @GetMapping("get/io")
    public String ioAware() {
        return ioExecutor.getClass().getName();
    }

    @GetMapping("get/server")
    public String serverAware() {
        return server.getClass().getName();
    }

    @GetMapping("get/context")
    public String deployContextAware() {
        return ctx.getClass().getName();
    }
}
