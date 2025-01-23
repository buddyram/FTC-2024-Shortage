package com.buddyram.rframe;


import java.util.TreeMap;

public class SmartLogWrapper extends BaseLogger {
    private final TreeMap<String, Object> logs = new TreeMap<>();
    private final Logger logger;

    public SmartLogWrapper(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(String caption, Object value) {
        this.logs.put(caption, value);
        this.logAllAndFlush();
    }

    public void logAllAndFlush() {
        for (String i: logs.keySet()) {
            this.logger.log(i, this.logs.get(i));
        }
        this.logger.flush();
    }

    @Override
    public void flush() {
        this.logger.flush();
    }
}
