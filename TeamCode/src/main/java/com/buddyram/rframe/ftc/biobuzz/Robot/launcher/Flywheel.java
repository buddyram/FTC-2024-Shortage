package com.buddyram.rframe.ftc.biobuzz.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class Flywheel extends BaseComponent<Robot> {
    private final RPMMotor motor;
    private double target;
    public static final int MAX = 5000;
    public static final int MIN = 0;
    // At-speed band (+/- rpm). The hub reports speed in ~43 rpm steps, so keep this above ~22 or some targets never count as ready
    public static final int THRESHOLD = 30;
    public static final double DEFAULT_RPM = 2250; // starting speed for the manual / test opmodes
    // Velocity PIDF for RUN_USING_ENCODER, from the Flywheel PIDF Tuner: F measured by its F calibration,
    // I and D from its best 4-ball shot rounds (I helps the wheel recover between back-to-back shots).
    public static final PIDFCoefficients PIDF = new PIDFCoefficients(254.0, 0.5, 1.0, 14.418);

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
