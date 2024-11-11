package com.buddyram.rframe;

public interface Logger {
    public void log(String caption, Object value);
    public void flush();
}
