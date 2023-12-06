package core.framework.internal.async;

import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThread {
    public static final Stats STATS = new Stats();

    public static class Stats {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger maxCount = new AtomicInteger(0);

        public void increase() {
            int current = count.incrementAndGet();
            maxCount.getAndAccumulate(current, Math::max);     // only increase active request triggers max active request process, doesn't need to handle when active requests decrease
        }

        public int maxCount() {
            return maxCount.getAndSet(count.get());
        }

        public void decrease() {
            count.decrementAndGet();
        }
    }
}
