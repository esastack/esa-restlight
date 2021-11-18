/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.restlight.ext.filter.ipwhitelist;

import java.util.ArrayList;
import java.util.List;

public final class IpWhiteListOptionsConfigure {

    private List<String> ips = new ArrayList<>(0);
    private int cacheSize = 1024;
    private long expire = 60L * 1000;

    private IpWhiteListOptionsConfigure() {
    }

    public static IpWhiteListOptionsConfigure newOpts() {
        return new IpWhiteListOptionsConfigure();
    }

    public static IpWhiteListOptions defaultOpts() {
        return newOpts().configured();
    }

    public IpWhiteListOptionsConfigure ips(List<String> ips) {
        this.ips = ips;
        return this;
    }

    public IpWhiteListOptionsConfigure cacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public IpWhiteListOptionsConfigure expire(int expire) {
        this.expire = expire;
        return this;
    }

    public IpWhiteListOptions configured() {
        final IpWhiteListOptions options = new IpWhiteListOptions();
        options.setCacheSize(cacheSize);
        options.setExpire(expire);
        options.setIps(ips);
        return options;
    }
}
