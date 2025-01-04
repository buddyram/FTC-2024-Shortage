package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;

public class ArmPositionalAction extends BaseArmAction {
    int claw;
    private final double wrist;
    private final int extension;
    private final int angle;

    public ArmPositionalAction(int claw, double wrist, int extension, int angle) {
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
    }

    @Override
    public boolean runArm(RobotArm arm) {
        arm.angle.setTargetPosition(this.angle);
        arm.extension.setTargetPosition(this.extension);
        arm.wrist.setPosition(this.wrist);
        arm.claw.setPosition(this.claw);
        this.waitForCompletion();

        return true;
    }
}
