package com.alertmns.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public boolean allowRequest(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(now));

        synchronized (counter) {
            if (now - counter.windowStart >= window.toMillis()) {
                counter.windowStart = now;
                counter.count = 0;
            }

            counter.count++;
            pruneExpiredEntries(now, window.multipliedBy(4));
            return counter.count <= limit;
        }
    }

    private void pruneExpiredEntries(long now, Duration retention) {
        if (counters.size() < 1024) {
            return;
        }

        Iterator<Map.Entry<String, WindowCounter>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WindowCounter> entry = iterator.next();
            if (now - entry.getValue().windowStart >= retention.toMillis()) {
                iterator.remove();
            }
        }
    }

    private static final class WindowCounter {
        private long windowStart;
        private int count;

        private WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = 0;
        }
    }
}
