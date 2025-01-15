package com.buddyram.rframe.ftc.intothedeep.arm;


public class RobotArm {
    public final Shoulder angle;
    public final Extension extension;
    public final Wrist wrist;
    public final Claw claw;
    public final Elbow elbow;

    public RobotArm(Claw claw, Wrist wrist, Elbow elbow, Extension extension, Shoulder angle){
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
        this.elbow = elbow;
    }
}

