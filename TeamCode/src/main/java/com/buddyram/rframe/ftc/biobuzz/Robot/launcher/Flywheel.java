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
    public static final int THRESHOLD = 50;
    // Velocity PIDF for RUN_USING_ENCODER. P was 354 with the old flywheel; the new one is
    // about 1/4 the weight, so P is cut to 1/4 to stop it from yanking the wheel up to speed.
    // F (feedforward) is about motor speed, not weight, so it stays the same.
    public static final PIDFCoefficients PIDF = new PIDFCoefficients(254.0, 0, 0, 20);

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
