package com.buddyram.rframe;

public abstract class BaseLogger implements Logger {
    public <O> void track(String caption, Trackable<O> trackable, Robot robot) {
        Thread thread = new Thread(
                () -> {
                    try {
                        while (robot.isActive()) {
                            this.log(caption, trackable.get());
                            Thread.sleep(250);
                        }
                    } catch (InterruptedException ignored) {

                    }
                }
        );
        thread.start();
    }
}