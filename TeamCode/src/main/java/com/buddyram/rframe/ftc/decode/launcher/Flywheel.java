package com.buddyram.rframe.ftc.decode.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;

public class Flywheel extends BaseComponent<Robot> {
    private final RPMMotor motor;
    private double target;
    public static final int MAX = 4000;
    public static final int MIN = 0;
    public static final int THRESHOLD = 50;

    public Flywheel(Robot robot, RPMMotor motor) {
        super(robot);
        this.motor = motor;
    }

    public double getTarget() {
        return this.target;
    }

    public double getRPM() {
        return this.motor.getRPM();
    }

    public boolean isReady() {
        return Math.abs(this.target - this.motor.getRPM()) < THRESHOLD ;
    }

    public void setRPM(double newRPM) {
        if (newRPM > MAX || newRPM < MIN) {
            return;
        }
        this.target = newRPM;
        this.motor.setRPM(this.target);
    }
    public void increaseRPM(double deltaRPM) {
        this.setRPM(target + deltaRPM);
    }
    public static RobotAction<DecodeBot> setRPMTo(int tgt) {
        return (robot) -> {
            robot.getLauncher().wheel.setRPM(tgt);
            return true;
        };
    }
}
