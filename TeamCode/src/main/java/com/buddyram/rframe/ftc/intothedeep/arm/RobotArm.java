package com.buddyram.rframe.ftc.intothedeep.arm;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;

public class RobotArm extends BaseComponent<Robot> {
    public final Shoulder angle;
    public final Extension extension;
    public final Wrist wrist;
    public final Claw claw;
    public final Elbow elbow;

    public RobotArm(Claw claw, Wrist wrist, Elbow elbow, Extension extension, Shoulder angle, Robot robot) {
        super(robot);
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
        this.elbow = elbow;
    }
}

