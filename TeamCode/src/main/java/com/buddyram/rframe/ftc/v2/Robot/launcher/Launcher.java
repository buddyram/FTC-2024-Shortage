package com.buddyram.rframe.ftc.v2.Robot.launcher;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;

public class Launcher extends BaseComponent<Robot> {
    public final Flywheel wheel;
    public final Turret turret;
    public final Hood hood;

    public Launcher(Robot robot, Flywheel wheel, Turret turret, Hood hood) {
        super(robot);
        this.wheel = wheel;
        this.turret = turret;
        this.hood = hood;
    }
}