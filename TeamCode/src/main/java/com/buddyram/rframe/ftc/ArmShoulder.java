package com.buddyram.rframe.ftc;

import com.qualcomm.robotcore.hardware.DcMotor;

public class ArmShoulder {
    private final DcMotor angleL;
    private final DcMotor angleR;

    public ArmShoulder(DcMotor angleL, DcMotor angleR) {
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setTargetPosition(int tgt) {
        this.angleL.setTargetPosition(tgt);
        this.angleR.setTargetPosition(tgt / 3);
    }
}
