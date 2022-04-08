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
package io.esastack.restlight.starter.actuator.adapt;

import esa.commons.Checks;
import io.esastack.commons.net.http.HttpMethod;
import io.esastack.restlight.server.bootstrap.WebServerException;
import io.esastack.restlight.server.context.RequestContext;
import io.esastack.restlight.server.core.HttpResponse;
import io.esastack.restlight.server.route.RouteRegistry;
import io.esastack.restlight.server.util.Futures;
import io.esastack.restlight.server.util.PathVariableUtils;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.invoke.MissingParametersException;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This handler is a fake handler for Restlight. {@link #handle(RequestContext, Map)} method will be
 * regarded as a controller interface to be registered into the {@link RouteRegistry}, and it is designed as a
 * asynchronously controller which always returns a {@link CompletionStage} result.
 */
class OperationHandler {

    private final WebOperation operation;

    OperationHandler(WebOperation operation) {
        Checks.checkNotNull(operation);
        this.operation = operation;
    }

    @SuppressWarnings("unused")
    CompletionStage<Object> handle(RequestContext context, Map<String, String> body) {
        return handleResult(doInvoke(context, body), context.response());
    }

    private Object doInvoke(RequestContext context, Map<String, String> body) {
        try {
            return this.operation.invoke(new InvocationContext(SecurityContext.NONE,
                    getArguments(context, body)));
        } catch (MissingParametersException e) {
            return Futures.completedExceptionally(WebServerException.badRequest(e));
        }
    }

    private Map<String, Object> getArguments(RequestContext context,
                                             Map<String, String> body) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        if (body != null && HttpMethod.POST.equals(context.request().method())) {
            arguments.putAll(body);
        }
        context.request().paramsMap().forEach((name, values) -> arguments.put(name, (values.size() != 1) ? values :
                values.get(0)));
        Map<String, String> urlTemplateVariables = PathVariableUtils.getPathVariables(context);
        if (urlTemplateVariables != null && !urlTemplateVariables.isEmpty()) {
            arguments.putAll(urlTemplateVariables);
        }
        return arguments;
    }

    private CompletionStage<Object> handleResult(Object result, HttpResponse res) {
        Object r;
        if (result instanceof WebEndpointResponse) {
            WebEndpointResponse<?> response = (WebEndpointResponse<?>) result;
            r = response.getBody();
            res.status(response.getStatus());
        } else {
            r = result;
        }
        return wrap(r);
    }

    @SuppressWarnings("unchecked")
    private CompletionStage<Object> wrap(Object obj) {
        if (obj instanceof CompletableFuture) {
            return (CompletableFuture<Object>) obj;
        }
        return Futures.completedFuture(obj);
    }

    @Override
    public String toString() {
        return this.operation.toString();
    }

}
