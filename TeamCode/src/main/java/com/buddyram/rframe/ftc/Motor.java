package com.buddyram.rframe.ftc;

import com.qualcomm.robotcore.hardware.DcMotor;

public class Motor implements com.buddyram.rframe.Motor {
    private final DcMotor motor;
    private double errorCorrection;

    public Motor(DcMotor motor) {
        this(motor, 1);
    }

    public Motor(DcMotor motor, double errorCorrection) {
        this.motor = motor;
        this.errorCorrection = errorCorrection;
    }

    public void setErrorCorrection(double errorCorrection) {
        this.errorCorrection = errorCorrection;
    }

    public double getErrorCorrection() {
        return errorCorrection;
    }

    public void setPower(double power) {
        this.motor.setPower(power * this.errorCorrection);

    }
}
