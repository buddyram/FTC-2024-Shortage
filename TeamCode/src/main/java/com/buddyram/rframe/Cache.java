package com.buddyram.rframe;

import java.util.function.Supplier;

public class Cache<T> {

    private T last;
    private long lastRefreshedAt;
    private final long maxAgeMillis;
    private final Supplier<T> loader;

    public Cache(long maxAgeMillis, Supplier<T> loader) {
        this.maxAgeMillis = maxAgeMillis;
        this.loader = loader;
    }

    public T refresh() {
        this.last = this.loader.get();
        this.lastRefreshedAt = System.currentTimeMillis();
        return this.last;
    }

    public T get() {
        return (System.currentTimeMillis() - lastRefreshedAt > maxAgeMillis) ? refresh() : last;
    }
}
