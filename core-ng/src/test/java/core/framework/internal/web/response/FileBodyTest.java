package core.framework.internal.web.response;

import core.framework.log.ErrorCode;
import core.framework.log.Severity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class FileBodyTest {
    @Test
    void convertException() {
        var channel = mock(FileChannel.class);
        var callback = new FileBody.FileBodyCallback(channel);

        UncheckedIOException exception = callback.convertException(new ClosedChannelException());
        assertThat(exception)
            .isInstanceOf(FileBody.ClientAbortException.class)
            .isInstanceOf(ErrorCode.class)
            .satisfies(error -> assertThat(((ErrorCode) error).severity()).isEqualTo(Severity.WARN));

        /*
        java.io.UncheckedIOException: java.io.IOException: Connection reset by peer
            at core.framework.internal.web.response.FileBody$FileBodyCallback.convertException(FileBody.java:70)
            at core.framework.internal.web.response.FileBody$FileBodyCallback.onException(FileBody.java:62)
            at io.undertow.io.AsyncSenderImpl.invokeOnException(AsyncSenderImpl.java:489)
            at io.undertow.io.AsyncSenderImpl$TransferTask.run(AsyncSenderImpl.java:108)
            at io.undertow.io.AsyncSenderImpl$TransferTask.run(AsyncSenderImpl.java:123)
            at io.undertow.io.AsyncSenderImpl.transferFrom(AsyncSenderImpl.java:301)
            at core.framework.internal.web.response.FileBody.send(FileBody.java:38)
            at core.framework.internal.web.response.ResponseHandler.render(ResponseHandler.java:49)
            at core.framework.internal.web.HTTPHandler.handle(HTTPHandler.java:118)
            at core.framework.internal.web.HTTPHandler.lambda$handle$1(HTTPHandler.java:85)
            at java.base/jdk.internal.vm.ScopedValueContainer.runWithoutScope(Unknown Source)
            at java.base/jdk.internal.vm.ScopedValueContainer.run(Unknown Source)
            at java.base/java.lang.ScopedValue$Carrier.run(Unknown Source)
            at core.framework.internal.web.controller.WebContextImpl.run(WebContextImpl.java:47)
            at core.framework.internal.web.HTTPHandler.lambda$handle$0(HTTPHandler.java:81)
            at core.framework.internal.log.LogManager.lambda$run$0(LogManager.java:65)
            at java.base/jdk.internal.vm.ScopedValueContainer.callWithoutScope(Unknown Source)
            at java.base/jdk.internal.vm.ScopedValueContainer.call(Unknown Source)
            at java.base/java.lang.ScopedValue$Carrier.call(Unknown Source)
            at core.framework.internal.log.LogManager.run(LogManager.java:63)
            at core.framework.internal.web.HTTPHandler.handle(HTTPHandler.java:79)
            at core.framework.internal.web.HTTPHandler.handleRequest(HTTPHandler.java:70)
            at io.undertow.server.Connectors.executeRootHandler(Connectors.java:418)
            at io.undertow.server.HttpServerExchange$1.run(HttpServerExchange.java:938)
            at java.base/java.util.concurrent.ThreadPerTaskExecutor$TaskRunner.run(Unknown Source)
            at java.base/java.lang.VirtualThread.run(Unknown Source)
        Caused by: java.io.IOException: Connection reset by peer
            at java.base/sun.nio.ch.SocketDispatcher.write0(Native Method)
            at java.base/sun.nio.ch.SocketDispatcher.write(Unknown Source)
            at java.base/sun.nio.ch.IOUtil.writeFromNativeBuffer(Unknown Source)
            at java.base/sun.nio.ch.IOUtil.write(Unknown Source)
            at java.base/sun.nio.ch.IOUtil.write(Unknown Source)
            at java.base/sun.nio.ch.SocketChannelImpl.implWrite(Unknown Source)
            at java.base/sun.nio.ch.SocketChannelImpl.write(Unknown Source)
            at org.xnio.nio.NioSocketConduit.write(NioSocketConduit.java:153)
            at io.undertow.protocols.ssl.SslConduit.doWrap(SslConduit.java:973)
            at io.undertow.protocols.ssl.SslConduit.write(SslConduit.java:399)
            at io.undertow.server.protocol.http.HttpResponseConduit.processWrite(HttpResponseConduit.java:291)
            at io.undertow.server.protocol.http.HttpResponseConduit.write(HttpResponseConduit.java:666)
            at io.undertow.conduits.ChunkedStreamSinkConduit.doWrite(ChunkedStreamSinkConduit.java:270)
            at io.undertow.conduits.ChunkedStreamSinkConduit.write(ChunkedStreamSinkConduit.java:129)
            at org.xnio.conduits.ConduitWritableByteChannel.write(ConduitWritableByteChannel.java:44)
            at java.base/sun.nio.ch.FileChannelImpl.transferToArbitraryChannel(Unknown Source)
            at java.base/sun.nio.ch.FileChannelImpl.transferTo(Unknown Source)
            at io.undertow.conduits.ChunkedStreamSinkConduit.transferFrom(ChunkedStreamSinkConduit.java:349)
            at org.xnio.conduits.ConduitStreamSinkChannel.transferFrom(ConduitStreamSinkChannel.java:142)
            at io.undertow.channels.DetachableStreamSinkChannel.transferFrom(DetachableStreamSinkChannel.java:127)
            at io.undertow.server.HttpServerExchange$WriteDispatchChannel.transferFrom(HttpServerExchange.java:2231)
            at io.undertow.io.AsyncSenderImpl$TransferTask.run(AsyncSenderImpl.java:94)
            ... 22 more
        */
        exception = callback.convertException(new IOException("Connection reset by peer"));
        assertThat(exception)
            .isInstanceOf(FileBody.ClientAbortException.class)
            .isInstanceOf(ErrorCode.class)
            .satisfies(error -> assertThat(((ErrorCode) error).severity()).isEqualTo(Severity.WARN));
    }
}
