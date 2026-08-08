package com.buddyram.rframe;

public class CachedOdometry<T> implements Odometry<T> {
    private Odometry<T> odometry;
    private final Cache<T> pos = new Cache<>(100, () -> odometry.get());

    public CachedOdometry(Odometry<T> odometry) {
        this.odometry = odometry;
    }

    public T refresh() {
        return pos.refresh();
    }

    public T get() {
        return pos.get();
    }

    public boolean init() {
        return odometry.init();
    }

    public void setPosition(T pos) {
        odometry.setPosition(pos);
    }

    public void cleanup() {
        odometry.cleanup();
    }
}
