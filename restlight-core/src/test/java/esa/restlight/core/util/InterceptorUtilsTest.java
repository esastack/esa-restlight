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
package esa.restlight.core.util;

import esa.commons.collection.MultiValueMap;
import esa.restlight.core.DeployContext;
import esa.restlight.core.annotation.Intercepted;
import esa.restlight.core.config.RestlightOptions;
import esa.restlight.core.handler.Handler;
import esa.restlight.core.handler.impl.RouteHandlerImpl;
import esa.restlight.core.interceptor.*;
import esa.restlight.core.method.HandlerMethod;
import esa.restlight.core.mock.MockContext;
import esa.restlight.server.route.Mapping;
import esa.restlight.test.mock.MockAsyncRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterceptorUtilsTest {

    private static final Subject SUBJECT = new Subject();

    @Test
    void testIsIntercepted() throws NoSuchMethodException {
        final HandlerMethod method1
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method1"), SUBJECT);
        assertFalse(InterceptorUtils.isIntercepted(method1));

        final HandlerMethod method2
                = HandlerMethod.of(Subject.class.getDeclaredMethod("method2"), SUBJECT);
        assertTrue(InterceptorUtils.isIntercepted(method2));
    }

    @Test
    void testParseIncludesOrExcludes() {
        assertArrayEquals(new String[]{"/foo", "/bar"},
                InterceptorUtils.parseIncludesOrExcludes(null, new String[]{"foo", "bar"}));
        assertArrayEquals(new String[]{"/foo", "/bar"},
                InterceptorUtils.parseIncludesOrExcludes("", new String[]{"foo", "bar"}));
        assertArrayEquals(new String[]{"/foo", "/bar"},
                InterceptorUtils.parseIncludesOrExcludes(null, new String[]{"/foo", "/bar"}));
        assertArrayEquals(new String[]{"/ctx/foo", "/ctx/bar"},
                InterceptorUtils.parseIncludesOrExcludes("ctx", new String[]{"/foo", "/bar"}));
        assertArrayEquals(new String[]{"/ctx/foo", "/ctx/bar"},
                InterceptorUtils.parseIncludesOrExcludes("/ctx", new String[]{"/foo", "/bar"}));
        assertArrayEquals(new String[]{"/ctx/foo", "/ctx/bar"},
                InterceptorUtils.parseIncludesOrExcludes("ctx/", new String[]{"/foo", "/bar"}));
        assertArrayEquals(new String[]{"/ctx/foo", "/ctx/bar"},
                InterceptorUtils.parseIncludesOrExcludes("/ctx/", new String[]{"/foo", "/bar"}));
    }

    @Test
    void testFilterEmptyInterceptor() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Mapping mapping = Mapping.get("/foo");
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);

        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, Collections.emptyList());
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty());
    }

    @Test
    void testFilterInternalInterceptor() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Mapping mapping = Mapping.get("/foo");
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);

        // InternalInterceptor always match to route and request
        final InterceptorFactory interceptor1 = InterceptorFactory.of(new InternalInterceptor() {
        });
        final InterceptorFactory interceptor2 = InterceptorFactory.of(new InternalInterceptor() {
        });
        final List<InterceptorFactory> interceptors = Arrays.asList(interceptor1, interceptor2);

        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors);
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertTrue(filtered.containsKey(InterceptorPredicate.ALWAYS));
        assertEquals(2, filtered.get(InterceptorPredicate.ALWAYS).size());
        MockAsyncRequest request1 = MockAsyncRequest.aMockRequest().build();
        assertTrue(filtered.values()
                .iterator()
                .next()
                .get(0)
                .predicate()
                .test(request1));
    }

    @Test
    void testFilterMappingInterceptor() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Mapping mapping = Mapping.get("/foo");
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);

        // MappingInterceptor will always match to route
        final InterceptorFactory interceptor1 = InterceptorFactory.of(request ->
                request.method() == HttpMethod.POST);
        final InterceptorFactory interceptor2 = InterceptorFactory.of(request ->
                request.method() == HttpMethod.POST);
        final InterceptorFactory interceptor3 = InterceptorFactory.of(request ->
                request.method() == HttpMethod.POST);
        final List<InterceptorFactory> interceptors2 = Arrays.asList(interceptor1, interceptor2, interceptor3);

        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors2);
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
        assertTrue(filtered.values()
                .iterator()
                .next()
                .get(0)
                .predicate()
                .test(MockAsyncRequest.aMockRequest().withMethod("POST").build()));
        assertFalse(filtered.values()
                .iterator()
                .next()
                .get(0)
                .predicate()
                .test(MockAsyncRequest.aMockRequest().withMethod("GET").build()));
    }

    @Test
    void testFilterRouteInterceptor() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Mapping mapping = Mapping.get("/foo");
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);

        // RouteInterceptor will always match to request
        final InterceptorFactory interceptor1 = InterceptorFactory.of((ctx12, route) -> false);
        final InterceptorFactory interceptor2 = InterceptorFactory.of((ctx12, route) -> true);
        final InterceptorFactory interceptor3 = InterceptorFactory.of((ctx12, route) -> true);
        final List<InterceptorFactory> interceptors2 = Arrays.asList(interceptor1, interceptor2, interceptor3);

        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors2);
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertTrue(filtered.containsKey(InterceptorPredicate.ALWAYS));
        assertEquals(2, filtered.get(InterceptorPredicate.ALWAYS).size());
        assertTrue(filtered.values()
                .iterator()
                .next()
                .get(0)
                .predicate()
                .test(MockAsyncRequest.aMockRequest().build()));

    }

    @Test
    void testFilterHandlerInterceptor() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Mapping mapping = Mapping.get("/foo");
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);

        // RouteInterceptor will always match to request
        final InterceptorFactory match1 = InterceptorFactory.of(new HandlerInterceptor() {
        });

        final InterceptorFactory match2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/bar"};
            }
        });
        final InterceptorFactory match3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }
        });
        final InterceptorFactory match4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/fo?"};
            }
        });
        final InterceptorFactory match5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f??"};
            }
        });
        final InterceptorFactory match6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f*"};
            }
        });
        final InterceptorFactory match7 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/**"};
            }
        });
        final InterceptorFactory match8 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo/bar"};
            }
        });
        final InterceptorFactory match9 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo/bar", "/foo?"};
            }
        });

        final InterceptorFactory unMatch1 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[0];
            }
        });
        final InterceptorFactory unMatch2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/bar"};
            }
        });
        final InterceptorFactory unMatch3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/bar", "/baz"};
            }
        });
        final InterceptorFactory unMatch4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo"};
            }
        });
        final InterceptorFactory unMatch5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo", "/bar"};
            }
        });

        final InterceptorFactory unMatch6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/b**"};
            }
        });
        final InterceptorFactory unMatch7 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/fo?"};
            }
        });
        final InterceptorFactory unMatch8 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/f??"};
            }
        });
        final InterceptorFactory unMatch9 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/f*"};
            }
        });

        final InterceptorFactory unMatch10 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/fo?"};
            }
        });

        final List<InterceptorFactory> matches = Arrays.asList(match1,
                match2,
                match3,
                match4,
                match5,
                match6,
                match7,
                match8,
                match9);

        final List<InterceptorFactory> unMatches = Arrays.asList(
                unMatch1,
                unMatch2,
                unMatch3,
                unMatch4,
                unMatch5,
                unMatch6,
                unMatch7,
                unMatch8,
                unMatch9,
                unMatch10);

        final List<InterceptorFactory> interceptors = new ArrayList<>(matches);
        interceptors.addAll(unMatches);
        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors);

        assertNotNull(filtered);
        assertEquals(matches.size(),
                filtered.values().stream().map(List::size).reduce(0, (l1, l2) -> l1 + l2).intValue());
    }

    @Test
    void testFilterHandlerInterceptor1() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);
        final Mapping mapping = Mapping.get("/{p}");

        // RouteInterceptor will always match to request
        final InterceptorFactory match1 = InterceptorFactory.of(new HandlerInterceptor() {
        });

        final InterceptorFactory match2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/bar"};
            }
        });
        final InterceptorFactory match3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }
        });
        final InterceptorFactory match4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/fo?"};
            }
        });
        final InterceptorFactory match5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f??"};
            }
        });
        final InterceptorFactory match6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f*"};
            }
        });
        final InterceptorFactory match7 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/**"};
            }
        });
        final InterceptorFactory match8 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo/bar"};
            }
        });

        final InterceptorFactory match9 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/fo?"};
            }
        });
        final InterceptorFactory match10 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/f??"};
            }
        });
        final InterceptorFactory match11 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/f*"};
            }
        });

        final InterceptorFactory unMatch1 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[0];
            }
        });
        final InterceptorFactory unMatch2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar"};
            }
        });
        final InterceptorFactory unMatch3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar", "/baz"};
            }
        });
        final InterceptorFactory unMatch4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/b**"};
            }
        });

        final InterceptorFactory unMatch5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/fo?"};
            }
        });

        final InterceptorFactory unMatch6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/**"};
            }
        });

        final List<InterceptorFactory> matches = Arrays.asList(match1,
                match2,
                match3,
                match4,
                match5,
                match6,
                match7,
                match8,
                match9,
                match10,
                match11);

        final List<InterceptorFactory> unMatches = Arrays.asList(
                unMatch1,
                unMatch2,
                unMatch3,
                unMatch4,
                unMatch5,
                unMatch6);

        final List<InterceptorFactory> interceptors = new ArrayList<>(matches);
        interceptors.addAll(unMatches);
        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors);

        assertNotNull(filtered);
        assertEquals(matches.size(),
                filtered.values().stream().map(List::size).reduce(0, (l1, l2) -> l1 + l2).intValue());

    }

    @Test
    void testFilterHandlerInterceptor2() throws NoSuchMethodException {
        final DeployContext<? extends RestlightOptions> ctx = MockContext.mock();
        final Handler handler = new RouteHandlerImpl(HandlerMethod.of(Subject.class.getDeclaredMethod("method2"),
                SUBJECT), true, null);
        final Mapping mapping = Mapping.get("/fo?");

        // RouteInterceptor will always match to request
        final InterceptorFactory match1 = InterceptorFactory.of(new HandlerInterceptor() {
        });

        final InterceptorFactory match2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/bar"};
            }
        });
        final InterceptorFactory match3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }
        });
        final InterceptorFactory match4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/fo?"};
            }
        });
        final InterceptorFactory match5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f??"};
            }
        });
        final InterceptorFactory match6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/f*"};
            }
        });
        final InterceptorFactory match7 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/**"};
            }
        });
        final InterceptorFactory match8 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/foo/bar"};
            }
        });

        final InterceptorFactory match9 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/fa?"};
            }
        });

        final InterceptorFactory match10 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] excludes() {
                return new String[]{"/f*"};
            }
        });

        final InterceptorFactory match11 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/fo?"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/f??"};
            }
        });

        final InterceptorFactory match12 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/fo?"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/foo"};
            }
        });

        final InterceptorFactory unMatch1 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[0];
            }
        });
        final InterceptorFactory unMatch2 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar"};
            }
        });
        final InterceptorFactory unMatch3 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar", "/baz"};
            }
        });
        final InterceptorFactory unMatch4 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/b**"};
            }
        });

        final InterceptorFactory unMatch5 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo/bar"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/fo?"};
            }
        });

        final InterceptorFactory unMatch6 = InterceptorFactory.of(new HandlerInterceptor() {
            @Override
            public String[] includes() {
                return new String[]{"/foo"};
            }

            @Override
            public String[] excludes() {
                return new String[]{"/**"};
            }
        });

        final List<InterceptorFactory> matches = Arrays.asList(match1,
                match2,
                match3,
                match4,
                match5,
                match6,
                match7,
                match8,
                match9,
                match10);

        final List<InterceptorFactory> unMatches = Arrays.asList(
                unMatch1,
                unMatch2,
                unMatch3,
                unMatch4,
                unMatch5,
                unMatch6);

        final List<InterceptorFactory> interceptors = new ArrayList<>(matches);
        interceptors.addAll(unMatches);
        final MultiValueMap<InterceptorPredicate, Interceptor> filtered =
                InterceptorUtils.filter(ctx, mapping, handler, interceptors);

        assertNotNull(filtered);
        assertEquals(matches.size(),
                filtered.values().stream().map(List::size).reduce(0, (l1, l2) -> l1 + l2).intValue());

    }

    private static class Subject {
        @Intercepted(false)
        void method1() {
        }

        void method2() {
        }
    }
}
