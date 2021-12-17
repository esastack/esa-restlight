package io.esastack.restlight.core.resolver.nav;

import io.esastack.restlight.core.context.RequestContext;
import io.esastack.restlight.core.method.Param;

public interface NameAndValueResolver {

    Object resolve(String name, RequestContext ctx);

    NameAndValue createNameAndValue(Param param);
}
