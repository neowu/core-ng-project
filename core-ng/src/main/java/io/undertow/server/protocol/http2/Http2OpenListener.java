/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.undertow.server.protocol.http2;

import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;
import io.undertow.UndertowOptions;
import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.protocols.http2.Http2Channel;
import io.undertow.server.ConnectorStatistics;
import io.undertow.server.DelegateOpenListener;
import io.undertow.server.HttpHandler;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.StreamConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Open listener for HTTP2 server
 *
 * @author Stuart Douglas
 */
// due to pull request https://github.com/undertow-io/undertow/pull/1024 is not merged yet, overwrite the patched file for now, and removed all unnecessary logic to simplify
// track change with https://github.com/undertow-io/undertow/blob/master/core/src/main/java/io/undertow/server/protocol/http2/Http2OpenListener.java
public final class Http2OpenListener implements ChannelListener<StreamConnection>, DelegateOpenListener {
    public static final String HTTP2 = "h2";
    private final Set<Http2Channel> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ByteBufferPool bufferPool;
    private final int bufferSize;
    private final String protocol;
    private volatile HttpHandler rootHandler;
    private volatile OptionMap undertowOptions;

    public Http2OpenListener(final ByteBufferPool pool, final OptionMap undertowOptions) {
        this(pool, undertowOptions, HTTP2);
    }

    public Http2OpenListener(final ByteBufferPool pool, final OptionMap undertowOptions, String protocol) {
        this.undertowOptions = undertowOptions;
        this.bufferPool = pool;
        PooledByteBuffer buf = pool.allocate();
        this.bufferSize = buf.getBuffer().remaining();
        buf.close();
        this.protocol = protocol;
    }

    @Override
    public void handleEvent(final StreamConnection channel, PooledByteBuffer buffer) {
        if (UndertowLogger.REQUEST_LOGGER.isTraceEnabled()) {
            UndertowLogger.REQUEST_LOGGER.tracef("Opened HTTP/2 connection with %s", channel.getPeerAddress());
        }

        //cool, we have a Http2 connection.
        Http2Channel http2Channel = new Http2Channel(channel, protocol, bufferPool, buffer, false, false, undertowOptions);
        Integer idleTimeout = undertowOptions.get(UndertowOptions.IDLE_TIMEOUT);
        if (idleTimeout != null && idleTimeout > 0) {
            http2Channel.setIdleTimeout(idleTimeout);
        }

        connections.add(http2Channel);
        http2Channel.addCloseTask(connections::remove);
        http2Channel.getReceiveSetter().set(new Http2ReceiveListener(rootHandler, getUndertowOptions(), bufferSize, null));
        http2Channel.resumeReceives();
    }

    @Override
    public void handleEvent(StreamConnection channel) {
        handleEvent(channel, null);
    }

    @Override
    public ConnectorStatistics getConnectorStatistics() {
        return null;
    }

    @Override
    public void closeConnections() {
        for (Http2Channel i : connections) {
            IoUtils.safeClose(i);
        }
    }

    @Override
    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public void setRootHandler(final HttpHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @Override
    public OptionMap getUndertowOptions() {
        return undertowOptions;
    }

    @Override
    public void setUndertowOptions(final OptionMap undertowOptions) {
        if (undertowOptions == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("undertowOptions");
        }
        this.undertowOptions = undertowOptions;
    }

    @Override
    public ByteBufferPool getBufferPool() {
        return bufferPool;
    }
}
