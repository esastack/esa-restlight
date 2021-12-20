package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;

public interface NameAndValueResolver {

    /**
     * Resolves method parameter into an argument value.
     *
     * @param name name of parameter
     * @param ctx  context of request
     * @return resolved
     */
    Object resolve(String name, RequestContext ctx);

    /**
     * Create an instance of {@link NameAndValue} for the parameter.
     *
     * @param param parameter
     * @return name and value
     */
    NameAndValue<?> createNameAndValue(Param param);
}
