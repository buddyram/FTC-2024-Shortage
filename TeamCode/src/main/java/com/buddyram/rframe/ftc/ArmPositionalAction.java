package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;

public class ArmPositionalAction extends BaseArmAction {
    int claw;
    final double wrist;
    final int extension;
    final int angle;
    final double elbow;

    public ArmPositionalAction(int claw, double wrist, double elbow, int extension, int angle) {
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
        this.elbow = elbow;
    }

    @Override
    public boolean runArm(RobotArm arm) {
        arm.angle.setTargetPosition(this.angle);
        arm.extension.setTargetPosition(this.extension);
        arm.wrist.setPosition(this.wrist);
        arm.claw.setPosition(this.claw);
        arm.elbow.setPosition(this.elbow);
        return true;
    }
}
