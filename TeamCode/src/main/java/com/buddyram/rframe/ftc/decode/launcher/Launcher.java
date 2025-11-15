package com.buddyram.rframe.ftc.decode.launcher;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.decode.indexer.ColorSensor;

public class Launcher extends BaseComponent<Robot> {
    public final Flywheel wheel;
    public final Feeder feeder;
    public final ColorSensor sensor;
    public final Turret turret;

    public Launcher(Robot robot, Flywheel wheel, Feeder feeder, ColorSensor sensor, Turret turret) {
        super(robot);
        this.feeder = feeder;
        this.wheel = wheel;
        this.turret = turret;
        this.sensor = sensor;
    }
}