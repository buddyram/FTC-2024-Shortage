package com.buddyram.rframe.ftc;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class ArmElbow {
    private final Servo angleL;
    private final Servo angleR;

    public ArmElbow(Servo angleL, Servo angleR) {
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setPosition(double tgt) {
        this.angleL.setPosition(1 - tgt);
        this.angleR.setPosition(tgt);
    }
}

