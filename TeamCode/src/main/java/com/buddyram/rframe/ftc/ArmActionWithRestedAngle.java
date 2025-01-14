package com.buddyram.rframe.ftc;

public class ArmActionWithRestedAngle extends ArmPositionalAction {

    public ArmActionWithRestedAngle(double wrist, double elbow, int extension) {
        super(wrist, elbow, extension, 0);
    }

    @Override
    public boolean runArm(RobotArm arm) {
        super.runArm(arm);
        // TODO rest

        return true;
    }
}
