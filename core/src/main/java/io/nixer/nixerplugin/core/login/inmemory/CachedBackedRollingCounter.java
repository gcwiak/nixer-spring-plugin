package io.nixer.nixerplugin.core.login.inmemory;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedList;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.util.Assert;

/**
 * Sliding window counter implementation. Tracks individually every occurrence per key.
 * Internally creates independent counter for each key and stores them in Cache.
 * Housekeeping of count occurrences happens on sum() call.
 *
 */
@ThreadSafe
public class CachedBackedRollingCounter implements RollingCounter {

    private final LoadingCache<String, Timestamps> counts;

    private Clock clock = Clock.systemDefaultZone();

    private final long windowInMillis;

    public CachedBackedRollingCounter(final Duration windowInMillis) {
        Assert.notNull(windowInMillis, "Window must not be null");
        this.windowInMillis = windowInMillis.toMillis();
        this.counts = CacheBuilder.newBuilder()
                .expireAfterAccess(windowInMillis.plusMinutes(1))
                .build(new CacheLoader<String, Timestamps>() {
                    @Override
                    public Timestamps load(final String key) throws Exception {
                        return new Timestamps();
                    }
                });
    }

    @Override
    public void increment(final String key) {
        Assert.notNull(key, "Key must not be null");

        final long millis = clock.millis();
        final Timestamps timestamps = counts.getUnchecked(key);
        timestamps.add(millis);
    }

    @Override
    public void remove(final String key) {
        Assert.notNull(key, "Key must not be null");

        counts.invalidate(key);
    }

    @Override
    public int count(final String key) {
        Assert.notNull(key, "Key must not be null");

        final Timestamps timestamps = counts.getIfPresent(key);
        if (timestamps != null) {
            final int size = timestamps.size();
            if (size == 0) {
                counts.invalidate(key);
            }
            return size;
        } else {
            return 0;
        }
    }

    @VisibleForTesting
    public long keysSize() {
        return counts.size();
    }

    private class Timestamps {

        private final LinkedList<Long> timestamps = new LinkedList<>();

        synchronized void add(long timestamp) {
            timestamps.addLast(timestamp);
        }

        synchronized int size() {
            final long now = CachedBackedRollingCounter.this.clock.millis();
            if (timestamps.isEmpty()) {
                return 0;
            }
            long expireOlderThan = now - CachedBackedRollingCounter.this.windowInMillis;
            if (expireOlderThan > timestamps.getLast()) {
                return 0; // expired
            } else {
                removeExpired(expireOlderThan);
                return timestamps.size();
            }
        }

        private void removeExpired(final long expireOlderThan) {
            while (true) {
                Long time = timestamps.getFirst();
                if (time == null) {
                    return;
                } else if (time < expireOlderThan) {
                    timestamps.removeFirst();
                } else {
                    break;
                }
            }
        }
    }

    public void setClock(final Clock clock) {
        Assert.notNull(clock, "Clock must not be null");

        this.clock = clock;
    }
}
