package core.framework.impl.resource;

import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * @author neo
 */
class PoolTest {
    private Pool<TestPoolResource> pool;

    @BeforeEach
    void createPool() {
        pool = new Pool<>(TestPoolResource::new, "pool");
        pool.maxIdleTime = Duration.ZERO;
        pool.checkoutTimeout(Duration.ZERO);
    }

    @Test
    void borrowAndReturn() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        assertThat(item.resource).isNotNull();
        pool.returnItem(item);

        assertThat(pool.idleItems.size()).isEqualTo(1);
        assertThat(pool.size.get()).isEqualTo(1);
        assertThat(pool.idleItems.getFirst().returnTime).isGreaterThan(0);
    }

    @Test
    void returnBrokenResource() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        assertThat(item.resource).isNotNull();
        assertThat(pool.size.get()).isEqualTo(1);

        item.broken = true;
        pool.returnItem(item);

        assertThat(pool.idleItems.size()).isZero();
        assertThat(pool.size.get()).isZero();
        assertThat(item.resource.closed).isTrue();
    }

    @Test
    void refresh() {
        pool.size(2, 2);

        pool.refresh();
        assertThat(pool.size.get()).isEqualTo(2);
        assertThat(pool.idleItems.size()).isEqualTo(2);
    }

    @Test
    void refreshWithRecycle() {
        pool.size(1, 5);

        List<PoolItem<TestPoolResource>> items = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            items.add(pool.borrowItem());
        }
        items.forEach(pool::returnItem);

        pool.refresh();
        assertThat(pool.size.get()).isEqualTo(1);
        assertThat(pool.idleItems.size()).isEqualTo(1);
    }

    @Test
    void borrowWithTimeout() {
        pool.size(0, 0);
        PoolException exception = catchThrowableOfType(() -> pool.borrowItem(), PoolException.class);
        assertThat(exception.errorCode()).isEqualTo("POOL_TIME_OUT");
    }

    @Test
    void close() {
        PoolItem<TestPoolResource> item = pool.borrowItem();
        pool.returnItem(item);

        pool.close();
        assertThat(item.resource.closed).isTrue();
    }
}
