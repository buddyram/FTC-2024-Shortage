package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;

public class ArmPositionalAction extends BaseArmAction {
    final double wrist;
    final int extension;
    final int angle;
    final double elbow;

    public ArmPositionalAction(double wrist, double elbow, int extension, int angle) {
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
        arm.elbow.setPosition(this.elbow);
        return true;
    }
}
