package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.BaseArmAction;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


public class RobotArm {
    public final ArmShoulder angle;
    public final DcMotor extension;
    public final Servo wrist;
    public final Servo claw;
    public final ArmElbow elbow;

    public RobotArm(Servo claw, Servo wrist, ArmElbow elbow, DcMotor extension, ArmShoulder angle){
        this.claw = claw;
        this.wrist = wrist;
        this.extension = extension;
        this.angle = angle;
        this.elbow = elbow;
    }
}

