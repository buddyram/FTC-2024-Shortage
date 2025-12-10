package com.buddyram.rframe.ftc;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;


public class RPMMotor {
//    private int lastCurrentPosition;
//    private long lastTimeNS;
    private final int countsPerRevolution;
    private DcMotorEx motor;

    public RPMMotor(DcMotorEx motor, int countsPerRevolution) {
        this.motor = motor;
        this.countsPerRevolution = countsPerRevolution;
    }

    public double getRPM() {
//        this.motor.
//        int StepsPerNS = (this.getCurrentPosition() - lastCurrentPosition);
        return this.motor.getVelocity() / (countsPerRevolution) * 60;
    }
    public void setRPM(double target) {
        this.motor.setVelocity(target * countsPerRevolution / 60);
    }
}
