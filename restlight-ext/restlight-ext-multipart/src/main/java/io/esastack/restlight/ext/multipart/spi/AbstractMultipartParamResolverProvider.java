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
package io.esastack.restlight.ext.multipart.spi;

import esa.commons.StringUtils;
import io.esastack.restlight.core.DeployContext;
import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.core.spi.ParamResolverProvider;
import io.esastack.restlight.ext.multipart.core.MultipartConfig;

import java.nio.charset.Charset;
import java.util.Optional;

abstract class AbstractMultipartParamResolverProvider implements ParamResolverProvider {

    private static final String ENCODING_KEY = "multipart.charset";
    private static final String USE_DISK_KEY = "multipart.use-disk";
    private static final String MEM_THRESHOLD_KEY = "multipart.memory-threshold";
    private static final String MAX_SIZE_KEY = "multipart.max-size";
    private static final String TEMP_DIR_KEY = "multipart.temp-dir";

    MultipartConfig buildConfig(DeployContext<? extends RestlightOptions> ctx) {
        MultipartConfig config;

        // Try to get useDiskValue from options, if absent then try to get minSizeValue.
        final Optional<String> useDiskValue = ctx.options().extOption(USE_DISK_KEY);
        config = useDiskValue.map(s -> new MultipartConfig(Boolean.parseBoolean(s)))
                .orElseGet(() -> new MultipartConfig(ctx.options()
                        .extOption(MEM_THRESHOLD_KEY)
                        .map(Long::valueOf)
                        .orElse(0L)));

        ctx.options().extOption(MAX_SIZE_KEY).ifPresent((s) -> config.setMaxSize(Long.parseLong(s)));
        ctx.options().extOption(ENCODING_KEY).ifPresent((s) -> config.setCharset(Charset.forName(s)));
        ctx.options().extOption(TEMP_DIR_KEY).ifPresent((s) -> config.setTempDir(StringUtils.trim(s)));
        return config;
    }

}
