package com.buddyram.rframe.ftc.v2.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

public class Turret extends BaseComponent<Robot> {
    public static final int MIN_ANGLE = -180;
    public static final int MAX_ANGLE = 180;
    public static final int GEAR_TEETH = 363;
    public static final int PULLEY_TEETH = 20;
    public static final int ENCODER_TICKS_PER_REV = 28;
    public static final double TICKS_PER_DEGREE = ENCODER_TICKS_PER_REV * GEAR_TEETH / (PULLEY_TEETH * 360.0);

    private static final double MAX_POWER = 0.6;
    private static final double MIN_POWER = 0.3;
    private static final double MAX_POWER_ANGLE = 0;
    private static final double MIN_POWER_ANGLE = 180;

    private final DcMotorEx motor;

    public Turret(Robot robot, DcMotorEx motor) {
        super(robot);
        this.motor = motor;
    }

    private int angleToTicks(double angle) {
        return (int) Math.floor(angle * TICKS_PER_DEGREE);
    }

    private double powerForAngleDelta(double angleDelta) {
        double absDelta = Math.abs(angleDelta);
        double t = Math.min(absDelta, MIN_POWER_ANGLE) / MIN_POWER_ANGLE;
        return MAX_POWER - t * (MAX_POWER - MIN_POWER);
    }

    public boolean isReady() {
        return Math.abs(motor.getTargetPosition() - motor.getCurrentPosition()) < 2;
    }

    public void setAngle(double newAngle) {
        if (newAngle > MIN_ANGLE && newAngle < MAX_ANGLE) {
            double currentAngle = motor.getCurrentPosition() / TICKS_PER_DEGREE;
            motor.setPower(powerForAngleDelta(newAngle - currentAngle));
            motor.setTargetPosition(angleToTicks(newAngle));
        }
    }
}
