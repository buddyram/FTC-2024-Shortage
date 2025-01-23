package com.buddyram.rframe;

public interface Logger {
    public void log(String caption, Object value);
    public void flush();
    <O> void track(String caption, Trackable<O> trackable, Robot robot);

    public interface Trackable<T> {
        public T get();
    }
}
