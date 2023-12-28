package core.framework.internal.stat;

import java.util.concurrent.atomic.AtomicInteger;

// to track max count between collecting
public class Counter {
    final AtomicInteger count = new AtomicInteger(0);
    final AtomicInteger max = new AtomicInteger(0);

    public void increase() {
        int current = count.incrementAndGet();
        max.getAndAccumulate(current, Math::max);     // only increase() may change max, no need to handle when decrease()
    }

    public int get() {
        return count.get();
    }

    public int max() {
        return max.getAndSet(count.get());
    }

    public int decrease() {
        return count.decrementAndGet();
    }
}
