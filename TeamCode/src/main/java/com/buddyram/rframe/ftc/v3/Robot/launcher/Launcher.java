package com.buddyram.rframe.ftc.v3.Robot.launcher;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;

public class Launcher extends BaseComponent<Robot> {
    public final Flywheel wheel;
    public final Turret turret;
    public final Hood hood;
    public final Blocker blocker;

    public Launcher(Robot robot, Flywheel wheel, Turret turret, Hood hood, Blocker blocker) {
        super(robot);
        this.wheel = wheel;
        this.turret = turret;
        this.hood = hood;
        this.blocker = blocker;
    }
}