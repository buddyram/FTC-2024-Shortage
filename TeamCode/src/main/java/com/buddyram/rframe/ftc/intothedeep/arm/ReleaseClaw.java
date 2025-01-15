package com.buddyram.rframe.ftc.intothedeep.arm;

public class ReleaseClaw extends BaseArmAction {
    private final int state;
    public ReleaseClaw(int state) {
        //super(0,0,0,0);
        this.state = state;
    }

    public boolean runArm(RobotArm arm) {
        arm.claw.setPosition(state);
        return true;
    }
}
