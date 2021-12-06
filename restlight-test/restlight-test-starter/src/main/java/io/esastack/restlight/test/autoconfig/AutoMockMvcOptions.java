package io.esastack.restlight.test.autoconfig;

import io.esastack.restlight.core.config.RestlightOptions;
import io.esastack.restlight.starter.autoconfigure.AutoRestlightServerOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = AutoRestlightServerOptions.PREFIX)
public class AutoMockMvcOptions extends RestlightOptions {
}
