package com.buddyram.rframe;

public class BaseComponent<R extends Robot> {
    private R robot;

    public BaseComponent(R robot) {
        this.robot = robot;
    }

    public R getRobot() {
        return robot;
    }

    public void setRobot(R robot) {
        this.robot = robot;
    }
}
