package core.framework.internal.http;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * @author neo
 */
public class ByteArrayBodyHandler implements HttpResponse.BodyHandler<byte[]> {
    @Override
    public HttpResponse.BodySubscriber<byte[]> apply(HttpResponse.ResponseInfo response) {
        // due to JDK http client bug: https://bugs.openjdk.java.net/browse/JDK-8211437
        // if response doesn't have content-length, http client will hang until connection closed by server side (after keep alive timeout)
        // here explicitly discard body if is no content
        if (response.statusCode() == 204) return new EmptyBodySubscriber();
        return HttpResponse.BodySubscribers.ofByteArray();
    }

    class EmptyBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
        }

        @Override
        public void onNext(List<ByteBuffer> items) {
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return CompletableFuture.completedStage(new byte[0]);
        }
    }
}
