package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.ftc.ArmAction;

public abstract class BaseArmAction implements ArmAction {

    @Override
    public boolean run(AutonomousDrive drive) {
        return this.runArm(drive.arm);
    }

    public void waitForCompletion() {
        try {
            Thread.sleep(1000);
        } catch(Exception ex) {}
    }

}
