package com.buddyram.rframe.ftc;

import com.qualcomm.robotcore.hardware.DcMotor;

public class EncoderMotor extends Motor {
//    private int lastCurrentPosition;
    private int zero;

    public EncoderMotor(DcMotor motor) {
        super(motor);
        this.zero = 0;
    }
    public void setZero(int newZero) {
        this.zero = newZero;
    }

    public int getCurrentPosition() {
        return this.motor.getCurrentPosition() - zero;
    }

    public int getZero() {
        return this.zero;
    }
}
