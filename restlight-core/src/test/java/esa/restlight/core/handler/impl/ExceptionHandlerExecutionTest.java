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
package esa.restlight.core.handler.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import esa.restlight.core.method.InvocableMethod;
import esa.restlight.core.method.MethodParam;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.SucceededFuture;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ExceptionHandlerExecutionTest {

    @Test
    void testResolveFixArg() throws NoSuchMethodException {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final InvocableMethod m = mock(InvocableMethod.class);
        when(m.method()).thenReturn(ExceptionHandlerExecutionTest.class.getDeclaredMethod("normal"));
        when(mock.handler()).thenReturn(m);
        final Throwable t = new Error();
        final ExceptionHandlerExecution execution = new ExceptionHandlerExecution(mock, t);
        final MethodParam p = mock(MethodParam.class);
        when(p.type()).thenReturn((Class) Error.class);
        assertEquals(t, execution.resolveFixedArg(p, null, null));
        reset(p);
        when(p.type()).thenReturn((Class) RuntimeException.class);
        assertNull(execution.resolveFixedArg(p, null, null));
    }

    @Test
    void testTransferFutureWithNormalReturn() throws NoSuchMethodException {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final InvocableMethod m = mock(InvocableMethod.class);
        when(m.method()).thenReturn(ExceptionHandlerExecutionTest.class.getDeclaredMethod("normal"));
        when(mock.handler()).thenReturn(m);
        final ExceptionHandlerExecution execution = new ExceptionHandlerExecution(mock, new Error());
        final Object obj = new Object();
        assertEquals(obj, execution.transferToFuture(obj).join());
    }

    @Test
    void testTransferFutureWithCf() throws NoSuchMethodException {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final InvocableMethod m = mock(InvocableMethod.class);
        when(m.method()).thenReturn(ExceptionHandlerExecutionTest.class.getDeclaredMethod("cf"));
        when(mock.handler()).thenReturn(m);
        final ExceptionHandlerExecution execution = new ExceptionHandlerExecution(mock, new Error());
        assertEquals("foo", execution.transferToFuture(CompletableFuture.completedFuture("foo")).join());
    }

    @Test
    void testTransferFutureWithGuavaFuture() throws NoSuchMethodException {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final InvocableMethod m = mock(InvocableMethod.class);
        when(m.method()).thenReturn(ExceptionHandlerExecutionTest.class.getDeclaredMethod("guava"));
        when(mock.handler()).thenReturn(m);
        final ExceptionHandlerExecution execution = new ExceptionHandlerExecution(mock, new Error());
        assertEquals("foo", execution.transferToFuture(Futures.immediateFuture("foo")).join());
    }

    @Test
    void testTransferFutureWithNettyFuture() throws NoSuchMethodException {
        final HandlerAdapter mock = mock(HandlerAdapter.class);
        final InvocableMethod m = mock(InvocableMethod.class);
        when(m.method()).thenReturn(ExceptionHandlerExecutionTest.class.getDeclaredMethod("netty"));
        when(mock.handler()).thenReturn(m);
        final ExceptionHandlerExecution execution = new ExceptionHandlerExecution(mock, new Error());
        final DefaultEventExecutor executor = new DefaultEventExecutor();
        assertEquals("foo",
                execution.transferToFuture(new SucceededFuture<>(executor, "foo")).join());
        executor.shutdownGracefully();
    }

    private Object normal() {
        return null;
    }

    private CompletableFuture<String> cf() {
        return null;
    }

    private ListenableFuture<String> guava() {
        return null;
    }

    private Future<String> netty() {
        return null;
    }


}
