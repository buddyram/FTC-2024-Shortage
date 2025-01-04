package com.buddyram.rframe.ftc;

public class ArmActionWithRestedAngle extends ArmPositionalAction {

    public ArmActionWithRestedAngle(int claw, double wrist, int extension) {
        super(claw, wrist, extension, 0);
    }

    @Override
    public boolean runArm(RobotArm arm) {
        super.runArm(arm);
        // TODO rest

        return true;
    }
}
