package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public abstract class BaseArmAction implements ArmAction {

    @Override
    public boolean run(AutonomousDrive drive) {
        return this.runArm(drive.arm);
    }
}
