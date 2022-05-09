package io.esastack.restlight.core.handler.impl;

import io.esastack.commons.net.http.HttpStatus;
import io.esastack.restlight.core.handler.RouteMethodInfo;
import io.esastack.restlight.core.method.RouteHandlerMethod;

public class RouteMethodInfoImpl extends HandlerMethodInfoImpl implements RouteMethodInfo {

    public RouteMethodInfoImpl(RouteHandlerMethod handlerMethod,
                           boolean locator,
                           HttpStatus customStatus) {
        super(handlerMethod, locator, customStatus);
    }

    @Override
    public RouteHandlerMethod handlerMethod() {
        return (RouteHandlerMethod) super.handlerMethod();
    }
}
