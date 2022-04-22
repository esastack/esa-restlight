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

package io.esastack.restlight.integration.test;

import io.esastack.restclient.RestResponseBase;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenglu
 */
public class AwareTest extends BaseIntegrationTest {

    @Test
    public void testBizAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/biz").execute()
                .toCompletableFuture().get();
        Assert.assertEquals(ThreadPoolExecutor.class.getName(), response.bodyToEntity(String.class));
    }

    @Test
    public void testIoAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/io").execute()
                .toCompletableFuture().get();
        Assert.assertEquals(NioEventLoopGroup.class.getName(), response.bodyToEntity(String.class));
    }

    @Test
    public void testServerAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/server").execute()
                .toCompletableFuture().get();
        Assert.assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("server"));
    }

    @Test
    public void testDeployContextAware() throws Exception {
        RestResponseBase response = restClient.get(domain + "/aware/get/context").execute()
                .toCompletableFuture().get();
        Assert.assertTrue(response.bodyToEntity(String.class).toLowerCase().contains("context"));
    }
}
