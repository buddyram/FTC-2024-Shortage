package com.buddyram.rframe.ftc.decode.launcher;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;

public class Launcher extends BaseComponent<Robot> {
    public final Flywheel wheel;
    public final Feeder feeder;

    public Launcher(Robot robot, Flywheel wheel, Feeder feeder) {
        super(robot);
        this.feeder = feeder;
        this.wheel = wheel;
    }
}