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
package io.esastack.restlight.server.route.predicate;

import esa.commons.Checks;
import esa.commons.UrlUtils;
import io.esastack.httpserver.core.HttpRequest;
import io.esastack.restlight.server.util.PathMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PatternsPredicate
 * <p>
 * Implementation of RequestPredicate which try to judge whether current request is matching;
 * <p>
 * use the PathMatcher to do the matching of the request and current predicate.
 */
public class PatternsPredicate implements RequestPredicate {

    public static final String TEMPLATE_VARIABLES = "$tmp.vars";

    private final PathMatcher[] patterns;
    private final boolean hasTemplateVar;

    public PatternsPredicate(String[] patterns) {
        Checks.checkNotNull(patterns, "patterns");
        this.patterns =
                UrlUtils.prependLeadingSlash(Arrays.asList(patterns))
                        .stream()
                        .map(PathMatcher::new)
                        .toArray(PathMatcher[]::new);
        this.hasTemplateVar = Arrays.stream(this.patterns).anyMatch(PathMatcher::isTemplateVarPattern);
    }

    @Override
    public boolean test(HttpRequest request) {
        if (this.patterns.length == 0) {
            return true;
        }
        // assume that if there's uri template in patterns the user will need the uri template variables which are
        // extracted from the path.
        Map<String, String> uriVariables = this.match(request.path());
        if (uriVariables != null) {
            //set matched template variables
            request.setAttribute(TEMPLATE_VARIABLES, uriVariables);
            return true;
        }
        return false;
    }

    /**
     * Get the matching patterns by the given lookup path
     *
     * @param lookupPath path
     *
     * @return uri template variables, none-{@code null} if given path matched, {@code null} if it did not.
     */
    private Map<String, String> match(String lookupPath) {
        if (hasTemplateVar) {
            // only matching with uri template variables if there's template in patterns.
            if (patterns.length == 1) {
                return patterns[0].matchAndExtractUriTemplateVariables(lookupPath);
            } else {
                Map<String, String> variables = null;
                for (PathMatcher p : patterns) {
                    if (variables == null) {
                        // first time or no pattern matched before.
                        variables = p.matchAndExtractUriTemplateVariables(lookupPath);
                    } else if (p.isTemplateVarPattern()) {
                        // match with uri template variables
                        Map<String, String> extracted = p.matchAndExtractUriTemplateVariables(lookupPath);
                        if (extracted != null && !extracted.isEmpty()) {
                            variables.putAll(extracted);
                        }
                    }
                    // there's no need to match the none-template variable pattern
                    // because variables is not null which means at lest one pattern has been matched to this path.
                }
                return variables;
            }
        } else {
            for (PathMatcher p : patterns) {
                if (!p.isPattern() && p.pattern().equals(lookupPath)) {
                    return Collections.emptyMap();
                }
                if (p.match(lookupPath)) {
                    return Collections.emptyMap();
                }
            }
            return null;
        }
    }

    @Override
    public boolean mayAmbiguousWith(RequestPredicate another) {
        if (this == another) {
            return true;
        }
        if (another == null || getClass() != another.getClass()) {
            return false;
        }
        PatternsPredicate that = (PatternsPredicate) another;
        String[] path = Arrays.stream(patterns).map(PathMatcher::pattern).toArray(String[]::new);
        String[] thatPath = Arrays.stream(that.patterns).map(PathMatcher::pattern).toArray(String[]::new);
        // path
        for (String p1 : path) {
            for (String p2 : thatPath) {
                if (PathMatcher.isPotentialIntersect(p1, p2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "{patterns=" + Arrays.stream(patterns)
                .map(PathMatcher::pattern)
                .collect(Collectors.joining(","))
                + '}';
    }
}
