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
package esa.restlight.ext.filter.xss;

import esa.httpserver.core.AsyncRequest;
import esa.restlight.server.handler.Filter;
import esa.restlight.server.handler.FilterChain;
import esa.restlight.server.util.Futures;
import esa.restlight.test.mock.MockAsyncRequest;
import esa.restlight.test.mock.MockAsyncResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class XssFilterTest {

    @Test
    void testDoFilter() {
        final AtomicReference<AsyncRequest> req = new AtomicReference<>();

        final FilterChain chain = ((request, response) -> {
            req.set(request);
            return Futures.completedFuture();
        });

        // filter mode
        final XssOptions options = XssOptionsConfigure.newOpts().mode(XssMode.FILTER).configured();
        final Filter filter = new XssFilter(options);
        AsyncRequest request0 = MockAsyncRequest.aMockRequest()
                .withUri("/test?script=<script>test</script>&foo=bar</script>&name=gcl&name=wxy")
                .withHeader("header", "src=\"//xxxx.cn/image/t.js\"")
                .build();
        filter.doFilter(request0, MockAsyncResponse.aMockResponse().build(), chain).join();
        assertEquals("", req.get().getParameter("script"));
        assertNull(req.get().getParameter("null"));
        assertEquals("", req.get().getHeader("header"));
        assertNull(req.get().getHeader("null"));
        assertEquals("script=&foo=bar&name=gcl&name=wxy", req.get().query());
        assertEquals("/test?script=&foo=bar&name=gcl&name=wxy", req.get().uri());
        assertEquals("/test", req.get().path());

        // test parameters & parameterMap
        String[] names = {"gcl", "wxy"};
        String[] namesForCompare = new String[names.length];
        String[] script = {""};
        String[] scriptForCompare = new String[script.length];
        assertArrayEquals(names, req.get().getParameters("name").toArray(scriptForCompare));
        assertNull(req.get().getParameters("null"));
        assertArrayEquals(names, req.get().parameterMap().get("name").toArray(namesForCompare));
        assertArrayEquals(script, req.get().parameterMap().get("script").toArray(scriptForCompare));

        String[] foos = {"bar"};
        String[] foosForCompare = new String[foos.length];
        assertArrayEquals(foos, req.get().parameterMap().get("foo").toArray(foosForCompare));

        // escape mode(default mode)
        options.setMode(XssMode.ESCAPE);
        final Filter filterEscape = new XssFilter(options);
        AsyncRequest requestEscape = MockAsyncRequest.aMockRequest()
                .withUri("/test?script=<script>test</script>&foo=test</script>&name=gcl&name=wxy")
                .withHeader("header", "src=\"//xxxx.cn/image/t.js\"")
                .build();
        filterEscape.doFilter(requestEscape, MockAsyncResponse.aMockResponse().build(), chain).join();
        assertEquals("&lt;script&gt;test&lt;/script&gt;", req.get().getParameter("script"));
        assertNull(req.get().getParameter("null"));
        assertEquals("src=&quot;//xxxx.cn/image/t.js&quot;", req.get().getHeader("header"));
        assertNull(req.get().getParameter("null"));
        assertEquals("script=&lt;script&gt;test&lt;/script&gt;&amp;foo=test&lt;/script&gt;&amp;name=gcl&amp;" +
                "name=wxy", req.get().query());
        assertEquals("/test?script=&lt;script&gt;test&lt;/script&gt;&amp;foo=test&lt;/script&gt;&amp;name=gcl&amp;" +
                "name=wxy", req.get().uri());
        assertEquals("/test", req.get().path());

        // test parameters & parameterMap
        names = new String[]{"gcl", "wxy"};
        namesForCompare = new String[names.length];
        script = new String[]{"&lt;script&gt;test&lt;/script&gt;"};
        scriptForCompare = new String[script.length];
        foos = new String[]{"test&lt;/script&gt;"};
        foosForCompare = new String[foos.length];
        assertArrayEquals(names, req.get().getParameters("name").toArray(scriptForCompare));
        assertNull(req.get().getParameters("null"));
        assertArrayEquals(names, req.get().parameterMap().get("name").toArray(namesForCompare));
        assertArrayEquals(script, req.get().parameterMap().get("script").toArray(scriptForCompare));
        assertArrayEquals(foos, req.get().parameterMap().get("foo").toArray(foosForCompare));
    }

    @Test
    void testDelegate() {
        final AsyncRequest delegate = mock(AsyncRequest.class);
        final XssFilter.FilterWrapper filter = new XssFilter.FilterWrapper(delegate);

        verify(delegate, never()).httpVersion();
        filter.httpVersion();
        verify(delegate).httpVersion();

        verify(delegate, never()).scheme();
        filter.scheme();
        verify(delegate).scheme();

        verify(delegate, never()).method();
        filter.method();
        verify(delegate).method();

        verify(delegate, never()).inputStream();
        filter.inputStream();
        verify(delegate).inputStream();

        verify(delegate, never()).byteBufBody();
        filter.byteBufBody();
        verify(delegate).byteBufBody();

        verify(delegate, never()).remoteAddr();
        filter.remoteAddr();
        verify(delegate).remoteAddr();

        verify(delegate, never()).tcpSourceAddr();
        filter.tcpSourceAddr();
        verify(delegate).tcpSourceAddr();

        verify(delegate, never()).remotePort();
        filter.remotePort();
        verify(delegate).remotePort();

        verify(delegate, never()).localAddr();
        filter.localAddr();
        verify(delegate).localAddr();

        verify(delegate, never()).localPort();
        filter.localPort();
        verify(delegate).localPort();

        verify(delegate, never()).headers();
        filter.headers();
        verify(delegate).headers();

        verify(delegate, never()).trailers();
        filter.trailers();
        verify(delegate).trailers();

        verify(delegate, never()).cookies();
        filter.cookies();
        verify(delegate).cookies();

        verify(delegate, never()).getAttribute("a");
        filter.getAttribute("a");
        verify(delegate).getAttribute("a");

        verify(delegate, never()).setAttribute("a", "b");
        filter.setAttribute("a", "b");
        verify(delegate).setAttribute("a", "b");

        verify(delegate, never()).removeAttribute("a");
        filter.removeAttribute("a");
        verify(delegate).removeAttribute("a");

        verify(delegate, never()).attributeNames();
        filter.attributeNames();
        verify(delegate).attributeNames();

        verify(delegate, never()).alloc();
        filter.alloc();
        verify(delegate).alloc();
    }
}
