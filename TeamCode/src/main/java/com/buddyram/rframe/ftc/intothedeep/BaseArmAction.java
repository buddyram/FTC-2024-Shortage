package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.ftc.ArmAction;

public abstract class BaseArmAction implements ArmAction {

    @Override
    public boolean run(AutonomousDrive drive) {
        return this.runArm(drive.arm);
    }
}
