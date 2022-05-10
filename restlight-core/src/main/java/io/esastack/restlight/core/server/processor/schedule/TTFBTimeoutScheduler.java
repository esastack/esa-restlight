/*
 * Copyright 2021 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restlight.core.server.processor.schedule;

import esa.commons.StringUtils;
import io.esastack.httpserver.utils.Constants;
import io.esastack.restlight.core.config.TimeoutOptions;
import io.esastack.restlight.core.util.LoggerUtils;

class TTFBTimeoutScheduler extends TimeoutScheduler {

    TTFBTimeoutScheduler(Scheduler scheduler, TimeoutOptions timeoutOptions) {
        super(scheduler, timeoutOptions);
    }

    @Override
    long getStartTime(RequestTask task) {
        String startTimeStr = task.request().headers().get(Constants.TTFB);
        if (StringUtils.isNotEmpty(startTimeStr)) {
            try {
                return Long.parseLong(startTimeStr);
            } catch (Exception ignore) {
            }
        }
        return System.currentTimeMillis();
    }

    @Override
    void schedule0(TimeoutRequestTask task) {
        long actualCost;
        if ((actualCost = System.currentTimeMillis() - task.startTime) < task.timeout) {
            super.schedule0(task);
        } else {
            task.failFast();
            LoggerUtils.logger().warn("Request(url = {}, method={}) has been rejected before submitting" +
                            " request task: Out of scheduler({}) timeout ({}ms), actual costs: {}ms",
                    task.request().path(),
                    task.request().rawMethod(),
                    task.schedulerName,
                    task.timeout,
                    actualCost);
        }

    }
}

