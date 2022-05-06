/*
 * Copyright 2022 OPPO ESA Stack Project
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
package io.esastack.restlight.server.mock;

import esa.commons.Checks;
import esa.commons.ExceptionUtils;
import esa.commons.io.IOUtils;
import io.esastack.commons.net.buffer.Buffer;
import io.esastack.restlight.core.resolver.ResponseContent;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MockResponseContent implements ResponseContent {

    private volatile Object committed;

    private static final AtomicReferenceFieldUpdater<MockResponseContent, Object> COMMITTED_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(MockResponseContent.class, Object.class, "committed");
    private static final Object END = new Object();
    private static final Object IDLE = new Object();

    private final Buffer buffer;

    public MockResponseContent(Buffer buffer) {
        Checks.checkNotNull(buffer, "buffer");
        this.buffer = buffer;
    }

    @Override
    public void write(byte[] data) {
        if (data == null) {
            return;
        }
        ensureCommitExclusively();
        try {
            buffer.writeBytes(data);
        } finally {
            // set to IDlE to indicates that the current write has been over and the next write is allowed.
            // we can use lazySet() because the IDLE value set here can be observed by other thread if the call of
            // write is kept in order which needs some means to ensure the memory visibility.
            COMMITTED_UPDATER.lazySet(this, IDLE);
        }
    }

    @Override
    public void end(byte[] data) {
        write(data);
        ensureEndExclusively(true);
    }

    @Override
    public void write(Buffer buffer) {
        if (buffer == null) {
            return;
        }
        ensureCommitExclusively();
        try {
            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            buffer.writeBytes(data);
        } finally {
            // set to IDlE to indicates that the current write has been over and the next write is allowed.
            // we can use lazySet() because the IDLE value set here can be observed by other thread if the call of
            // write is kept in order which needs some means to ensure the memory visibility.
            COMMITTED_UPDATER.lazySet(this, IDLE);
        }
    }

    @Override
    public void end(Buffer buffer) {
        write(buffer);
        ensureEndExclusively(true);
    }

    @Override
    public void end(File file) {
        try {
            write(IOUtils.toByteArray(file));
            ensureEndExclusively(false);
        } catch (IOException ex) {
            ExceptionUtils.throwException(ex);
        }
    }

    @Override
    public void end() {
        ensureEndExclusively(true);
    }

    @Override
    public boolean isCommitted() {
        return COMMITTED_UPDATER.get(this) != null;
    }

    @Override
    public boolean isEnded() {
        return COMMITTED_UPDATER.get(this) == END;
    }

    @Override
    public ByteBufAllocator alloc() {
        return new UnpooledByteBufAllocator(false);
    }

    private void ensureCommitExclusively() {
        final Object current = COMMITTED_UPDATER.get(this);
        if (current == null) {
            // INIT
            final Thread t = Thread.currentThread();
            if (COMMITTED_UPDATER.compareAndSet(this, null, t)) {
                return;
            }
            throw new IllegalStateException("Concurrent committing['INIT' -> '" + t.getName() + "']");
        } else if (current == END) {
            // END
            throw new IllegalStateException("Already ended");
        } else if (current == IDLE) {
            // IDLE
            final Thread t = Thread.currentThread();
            if (COMMITTED_UPDATER.compareAndSet(this, IDLE, t)) {
                return;
            }
            throw new IllegalStateException("Concurrent committing['IDLE' -> '" + t.getName() + "']");
        } else {
            // current response is being committing by another thread
            throw new IllegalStateException("Concurrent committing ['" + ((Thread) current).getName() +
                    "' -> '" + Thread.currentThread().getName() + "']");
        }
    }

    private void ensureEndExclusively(boolean allowCommitted) {
        final Object current = COMMITTED_UPDATER.get(this);
        if (current == null) {
            // INIT
            if (COMMITTED_UPDATER.compareAndSet(this, null, END)) {
                return;
            }
            throw new IllegalStateException("Concurrent ending['INIT' -> 'END']");
        } else if (current == IDLE) {
            // IDLE
            if (allowCommitted && COMMITTED_UPDATER.compareAndSet(this, IDLE, END)) {
                return;
            }
            throw new IllegalStateException("Concurrent ending['IDLE' -> 'END']");
        } else if (current == END) {
            // END
            throw new IllegalStateException("Already ended");
        } else {
            // current response is being committing by another thread
            throw new IllegalStateException("Concurrent ending['" + ((Thread) current).getName() + "' -> 'END']");
        }
    }

}


